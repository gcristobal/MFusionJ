import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.plugin.filter.*;

public class Decompose_ implements PlugInFilter
{
    ImagePlus imp;
    int height,width,bigWidth_g,bigHeight_g;
    int sigmaS=1,iter=3,size,type;
    float scale=0.5f;
    float gamma=1.2f;// 1 works better
    int nSlices,gvc;
    ImageStack stack;
    float gscale;
    float[] ha_kernel;

    @Override
    public int setup(String arg,ImagePlus imp) 
    {
        int a,i,kw;
        this.imp=imp;
        GenericDialog gd=new GenericDialog("Decompose");
        gd.addNumericField("SigmaS:",sigmaS,1);
        gd.addNumericField("Scale:",scale,1);
        gd.addNumericField("Iterations:",iter,1);
        gd.addNumericField("Gamma:",gamma,1);
        gd.showDialog();
        if (gd.wasCanceled())
            return DONE;
        sigmaS=(int)gd.getNextNumber();
        scale=(float)gd.getNextNumber();
        iter=(int)gd.getNextNumber();
        gamma=(float)gd.getNextNumber();
        stack=imp.getStack();
        nSlices=imp.getStackSize(); 
        width=imp.getWidth();
        height=imp.getHeight();
        size=width*height;
        type=imp.getType();
        //----------------------------------------------------------------------
        // GAUSSIAN,gets kernel
        String aux="";
        for (i=1;i<100;i++)
        {
            double gauss=Math.exp(-(i*i)/(2.*sigmaS*sigmaS));
            if (gauss>0.003)
                aux+=gauss+",";
            else
                break;
        }
        kw=6*sigmaS+1;
        gvc=kw/2;
        ha_kernel=new float[kw];  
        int centro=kw/2;
      //  double den = 0.0;
        //----------------------------------------------------------------------
        // Mascara gausiana y normalización
        for (i = 0;i<kw;i++)
        {
            float distanciaLado=i-centro;
            ha_kernel[i]=(float)Math.exp(-(distanciaLado*distanciaLado)/(2*sigmaS*sigmaS));
     //       den += ha_kernel[i];
        }
        // Normalización
     /*   for (i=0;i<kw;i++)
            ha_kernel[i]/=den;   */  
        bigWidth_g=width+kw-1;
        bigHeight_g=height+kw-1;
        return DOES_ALL-DOES_RGB;
    }

    @Override
    public void run(ImageProcessor ip)
    {
        int k;
        ImageStack BL_stack=new ImageStack(width,height);
        ImageStack DL_stack=new ImageStack(width,height);

        for (k=0;k<nSlices;k++)
        {
            int i,j,a,y;
            //----------------------------------------------------------------------
            float[] pixelsR=new float[size];
            switch(type)//Convert images to float
            {
                case 0://GRAY8
                    byte[]pixelsB=(byte[])stack.getProcessor(k+1).convertToByteProcessor().getPixels();
                    for(a=0;a<size;a++)
                        pixelsR[a]=(float)(pixelsB[a]&0xff);
                    break;
                case 1://GRAY16
                    short[]pixelsS=(short[])stack.getProcessor(k+1).convertToShortProcessor().getPixels();
                    for(a=0;a<size;a++)
                        pixelsR[a]=(float)pixelsS[a];
                    break;
                case 2://GRAY32
                    float[] pixelsF=(float[])stack.getProcessor(k+1).convertToFloat().getPixels();//(float[])ip.getPixels();
                    for(a=0;a<size;a++)
                        pixelsR[a]=pixelsF[a];
                    break;
            }
            float [] BL=new float[size];
            EPF(BL,pixelsR,pixelsR);
            float [] DL=new float[size];
            for(y=0;y<size;y++)
                DL[y]=(pixelsR[y]-BL[y])*gamma;
//            #Rolling Filter: BL now the input and I is acting as guidance image,which means guidance image is same and the input is changing in each iteration
//            for t in range(iter2-1):
//                for i in range(N):
//                    BL[:,:,:,i],_=EPF(BL[:,:,:,i],I[:,:,:,i],SigmaS1,scale1) # Base layer is computed based on EPF. Any other edge-preserving filter can be used e.g. Bilateral Filter,Guided Filter,Weighted Least Square Filter etc.
//                    diff=(I[:,:,:,i]-BL[:,:,:,i]) # Detail layer is computed by subtracting the filtered image from the input image.
//                    DL[:,:,:,i]=diff*gamma # Detail layer is multiplied with the user-defined parameter for detail enhancement,if required.
//            return BL,DL 
            if (iter>1)
            {
                for(i=1;i<iter;i++)
                    EPF(BL,BL,pixelsR);
                for(y=0;y<size;y++)
                    DL[y]=(pixelsR[y]-BL[y])*gamma;
            }
            // Output only floats
            // Add each BL and DL output for each image into the stack
            ImagePlus impBL32=NewImage.createFloatImage(null,width,height,1,NewImage.FILL_BLACK);
            ImagePlus impDL32=NewImage.createFloatImage(null,width,height,1,NewImage.FILL_BLACK);
            ImageProcessor ipBL32=impBL32.getProcessor();
            ImageProcessor ipDL32=impDL32.getProcessor();
            float[] bl_pixels32=(float[])ipBL32.getPixels();
            float[] dl_pixels32=(float[])ipDL32.getPixels();
            for(y=0;y<size;y++)
            {
                bl_pixels32[y]=(float)BL[y];
                dl_pixels32[y]=(float)DL[y];
            }
            BL_stack.addSlice("",ipBL32);
            DL_stack.addSlice("",ipDL32);
        } // end of the for loop for stack slices 
        // Create two new stack windows for BL and DL outputs and show
        ImagePlus impBL=new ImagePlus("Base Layer",BL_stack);
        IJ.resetMinAndMax(impBL);
        impBL.show();
        ImagePlus impDL=new ImagePlus("Detail Layer",DL_stack);
        IJ.resetMinAndMax(impDL);
        impDL.show();
    }

    public void EPF(float[] J,float[] imageG,float[] imageI)
    {
        // Inputs: J -- output of the function
        //    imageG -- guidance image G 
        //    imageI -- image I to be processed
        //    SigmaS -- bilateral spacial parameter,if SigmaS is large then larger scale objects are smoothed out.
        //     scale -- bilateral range parameter,if the scale<1 then sharper results are obtained.
        // width,height -- size of the G and I

        float sum;
        int a,b,i,j,offset,p,v,vc,x,y;
        // Calculate the patch size
        int patchSize=(int)(4*sigmaS+1);
        if (patchSize%2==0)
            patchSize++;
        // Calculate the size of the padding and the img size with padding
        vc=patchSize/2;
        int bigWidth=width+patchSize-1;
        int bigHeight=height+patchSize-1;
        //----------------------------------------------------------------------
        // muG=uniform_filter(G,size=patchSize,mode=padMethod)

        // PixelsG -> guidance image G,padded 
        float[][]pixelsG=new float[bigHeight][bigWidth];
        //Copy the pixels in the middle of the enlarged image
        for(a=y=0;y<height;y++)
            for(x=0;x<width;x++)
                pixelsG[y+vc][x+vc]=imageG[a++];
        //Mirroring-padding with the mirrored pixels (padMode=reflect)
        //Duplicate the pixels of the left and right parts of the image 
        for(i=vc;i<bigHeight-vc;i++)
        {
            j=vc*2-1;
            for(p=0;p<vc;p++)
                pixelsG[i][p]=pixelsG[i][j--];
            j=(bigWidth-2*vc);
            for(p=bigWidth-1;p>bigWidth-vc-1;p--)
                pixelsG[i][p]=pixelsG[i][j++];
        }
        //Duplicate the pixels of the up and down sides of the image
        for(i=0;i<bigWidth;i++)
        {
            j=vc*2-1;
            for(p=0;p<vc;p++)
                pixelsG[p][i]=pixelsG[j--][i];
            j=(bigHeight-2*vc);
            for(p=bigHeight-1;p>bigHeight-vc-1;p--)
                pixelsG[p][i]=pixelsG[j++][i];
        }
        //Convolve-Uniform Filter
        float[][] pixelsG2=new float[bigHeight][bigWidth];
        float[][] pixelsG_=new float[bigHeight][bigWidth];
        for(y=0;y<bigHeight;y++)
        //Convolve in the x axis
            for(x=vc;x<bigWidth-vc;x++)
            {
                for(sum=0,v=-vc;v<=vc;v++)
                    sum+=pixelsG[y][x+v];
                pixelsG2[y][x]=sum/patchSize;
            }
        for(x=0;x < bigWidth;x++)
        //Convolve in the y axis
            for(y=vc;y<bigHeight-vc;y++)
            {
                for(sum=0,v=-vc;v<=vc;v++)
                    sum +=pixelsG2[y+v][x];
                pixelsG_[y][x]=sum/patchSize;
            }
        //----------------------------------------------------------------------
        //muGG=uniform_filter(G*G,size=patchSize,mode=padMethod) # patch mean of G
        // Create G*G 
        float[][]pixelsGG=new float[bigHeight][bigWidth];
        //Copy the pixels in the middle of the enlarged image
        for(a=y=0;y<height;y++)
            for(x=0;x<width;x++)
                pixelsGG[y+vc][x+vc]=imageG[a]*imageG[a++];
        //Mirroring-padding with the mirrored pixels (padMode=reflect)
        //Duplicate the pixels of the left and right parts of the image 
        for(i=vc;i<bigHeight-vc;i++)
        {
            j=vc*2-1;
            for(p=0;p<vc;p++)
                pixelsGG[i][p]=pixelsGG[i][j--];
            j=(bigWidth-2*vc);
            for(p=bigWidth-1;p>bigWidth-vc-1;p--)
                pixelsGG[i][p]=pixelsGG[i][j++];
        }
        //Duplicate the pixels of the up and down sides of the image
        for(i=0;i<bigWidth;i++)
        {
            j=vc*2-1;
            for(p=0;p<vc;p++)
                pixelsGG[p][i]=pixelsGG[j--][i];
            j=(bigHeight-2*vc);
            for(p=bigHeight-1;p>bigHeight-vc-1;p--)
                pixelsGG[p][i]=pixelsGG[j++][i];
        }
        //Convolve-Uniform Filter
        float[][] pixelsGG2=new float[bigHeight][bigWidth];
        float[][] pixelsGG_=new float[bigHeight][bigWidth];
        for(y=0;y<bigHeight;y++)
            //Convolve in the x axis
            for(x=vc;x<bigWidth-vc;x++)
            {
                for(sum=0,v=-vc;v<=vc;v++)
                    sum+=pixelsGG[y][x+v];
                pixelsGG2[y][x]=sum/patchSize;
            }
        for(x=0;x<bigWidth;x++)
        //Convolve in the y axis
            for(y=vc;y<bigHeight-vc;y++)
            {
                for(sum=0,v=-vc;v<=vc;v++)
                    sum+=pixelsGG2[y+v][x];
                pixelsGG_[y][x]=sum/patchSize;
            }
        //----------------------------------------------------------------------
        // w=np.maximum(0,muGG-muG*muG)
        float[][] w=new float[bigHeight][bigWidth];
        for(y=0;y<bigHeight;y++)
            for(x=0;x<bigWidth;x++)
                w[y][x]=pixelsGG_[y][x]-(pixelsG_[y][x]*pixelsG_[y][x]);
        for(y=0;y<bigHeight;y++)
            for(x=0;x<bigWidth;x++)
                if (w[y][x]<0)
                    w[y][x]=0;
        // SigmaR=scale*np.mean(w)
        float w_sum=0.0f;
        float w_mean;
        for(y=vc;y<bigHeight-vc;y++)
            for(x=vc;x<bigWidth-vc;x++)
                w_sum+=w[y][x];
        w_mean=w_sum/size;
        float SigmaR=scale*w_mean;
        // w=w / SigmaR
        // w=1.0 / (1.0 + w ** 2)
        float[][] w_=new float[height][width]; 
        for(y=vc;y<bigHeight-vc;y++)
            for(x=vc;x<bigWidth-vc;x++)
                w_[y-vc][x-vc]=w[y][x]/SigmaR;
        for(y=0;y<height;y++)
            for(x=0;x<width;x++)
                w_[y][x]=1.f/(1.f+(w_[y][x]*w_[y][x]));
        //----------------------------------------------------------------------
        // ha=fspecial('gaussian',patchSize,SigmaS);
        // normalizeFactor=imfilter(w,ha,padMethod);
        float[][] w_padded=new float[bigHeight_g][bigWidth_g];
        //Copy the pixels in the middle of the enlarged image
        for(y=0;y<height;y++)
            for(x=0;x<width;x++)
                w_padded[y+gvc][x+gvc]=w_[y][x];
        //Mirroring-padding with the mirrored pixels
        //Duplicate the pixels of the left and right parts of the image 
        for(i=gvc;i<bigHeight_g-gvc;i++)
        {
            j=gvc*2-1;
            for(p=0;p<gvc;p++)
                w_padded[i][p]=w_padded[i][j--];
            j=(bigWidth_g-2*gvc);
            for(p=bigWidth_g-1;p>bigWidth_g-gvc-1;p--)
                w_padded[i][p]=w_padded[i][j++];
        }
        //Duplicate the pixels of the up and down sides of the image
        for(i=0;i<bigWidth_g;i++)
        {
            j=gvc*2-1;
            for(p=0;p<gvc;p++)
                w_padded[p][i]=w_padded[j--][i];
            j=(bigHeight_g-2*gvc);
            for(p=bigHeight_g-1;p>bigHeight_g-gvc-1;p--) 
                w_padded[p][i]=w_padded[j++][i];
        }
        float[] copy=new float[bigHeight_g*bigWidth_g];
                for(a=y=0;y<bigHeight_g;y++)
                    for(x=0;x<bigWidth_g;x++)
                        copy[a++]=w_padded[y][x];
        // Convolve Gaussian
        float[] pixels2=new float[bigHeight_g*bigWidth_g];
        float[] normalizeFactor=new float[size];
        for(y=gvc;y<bigHeight_g-gvc;y++)//Convolve in the x axis
        {
            offset=y*bigWidth_g;
            for(x=gvc;x<bigWidth_g-gvc;x++)
            {
                a=offset+x;
                for(b=0,sum=0,v=-gvc;v<=gvc;v++)
                    sum+=(copy[a+v])*(ha_kernel[b++]);
                pixels2[a]=sum/scale;
            }
        }
        for(y=gvc;y<bigHeight_g-gvc;y++)//Convolve in the y axis
        {
            offset=y*bigWidth_g;
            for(x=gvc;x<bigWidth_g-gvc;x++)
            {
                a=offset+x;
                for(b=0,sum=0,v=-gvc;v<=gvc;v++)
                    sum+=pixels2[a+v*bigWidth_g]*ha_kernel[b++];
                normalizeFactor[(y-gvc)*width+x-gvc]=sum/scale;
            }
        }
        //----------------------------------------------------------------------
        // J=imfilter(w.*I,ha,padMethod)./(eps+normalizeFactor) ;
        float[][] weighted_I=new float[bigHeight_g][bigWidth_g];
        // Copy the pixels in the middle of the enlarged image
        for(a=y=0;y<height;y++)
            for(x=0;x<width;x++)
                weighted_I[y+gvc][x+gvc]=imageI[a++]*w_[y][x];
        // Uniform filter on w*I
        //Mirroring-padding with the mirrored pixels
        //Duplicate the pixels of the left and right parts of the image 
        for(i=gvc;i<bigHeight_g-gvc;i++)
        {
            j=gvc*2-1;
            for(p=0;p<gvc;p++)
                weighted_I[i][p]=weighted_I[i][j--];
            j=(bigWidth_g-2*gvc);
            for(p=bigWidth_g-1;p>bigWidth_g-gvc-1;p--)
                weighted_I[i][p]=weighted_I[i][j++];
        }
        //Duplicate the pixels of the up and down sides of the image
        for(i=0;i<bigWidth_g;i++)
        {
            j=gvc*2-1;
            for(p=0;p<gvc;p++)
                weighted_I[p][i]=weighted_I[j--][i];
            j=(bigHeight_g-2*gvc);
            for(p=bigHeight_g-1;p>bigHeight_g-gvc-1;p--)
                weighted_I[p][i]=weighted_I[j++][i];
        } 
        float[] copy_wI=new float[bigHeight_g*bigWidth_g];
                for(a=y=0;y<bigHeight_g;y++)
                    for(x=0;x<bigWidth_g;x++)
                        copy_wI[a++]=weighted_I[y][x];
        // Convolve Gaussian
        float[] pixels3=new float[bigHeight_g*bigWidth_g];
        for(y=gvc;y<bigHeight_g-gvc;y++)//Convolve in the x axis
        {
            offset=y*bigWidth_g;
            for(x=gvc;x<bigWidth_g-gvc;x++)
            {
                a=offset+x;
                for(b=0,sum=0,v=-gvc;v<=gvc;v++)
                    sum+=copy_wI[a+v]*(ha_kernel[b++]);
                pixels3[a]=sum/scale;
            }
        }
        for(y=gvc;y<bigHeight_g-gvc;y++)//Convolve in the y axis
        {
            offset=y*bigWidth_g;
            for(x=gvc;x<bigWidth_g-gvc;x++)
            {
                a=offset+x;
                for(b=0,sum=0,v=-gvc;v<=gvc;v++)
                    sum+=pixels3[a+v*bigWidth_g]*ha_kernel[b++];
                J[(y-gvc)*width+x-gvc]=sum/scale;
            }
        }
        float epsilon=2.220446049250313e-16f;
        //Divide the filtered w*I by epsilon and then add normalization factor
        for(a=0;a<size;a++)
            J[a]/=(normalizeFactor[a]+epsilon);
    } 
}