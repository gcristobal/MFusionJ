import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.plugin.filter.*;

public class WMAP_ implements PlugInFilter
{
    ImagePlus imp;
    int bigHeight,height,bigWidth,width,bigWidth_g,bigHeight_g;
    int sigma1=8,sigma2=4,iter=3,size,type,vc,gvc,patchSize;
    float scale1=1f,scale2=0.5f;
    int nSlices;
    ImageStack stack;
    int a,i,j,x,y,p,v,offset,b,kw;
    float sum,gscale;
    float[] ha_kernel;

    @Override
    public int setup(String arg,ImagePlus imp) 
    {
        this.imp=imp;
        GenericDialog gd=new GenericDialog("WMAP");
        gd.addNumericField("Sigma BL:",sigma1,1);
        gd.addNumericField("Scale BL:",scale1,1);
        gd.addNumericField("Sigma DL:",sigma2,1);
        gd.addNumericField("Scale DL:",scale2,1);
        gd.addNumericField("Iterations:",iter,1);
        gd.showDialog();
        if (gd.wasCanceled())
            return DONE;
        sigma1=(int)gd.getNextNumber();
        scale1=(float)gd.getNextNumber();
        sigma2=(int)gd.getNextNumber();
        scale2=(float)gd.getNextNumber();
        iter=(int)gd.getNextNumber();
        stack=imp.getStack();
        nSlices=imp.getStackSize();
        width=imp.getWidth();
        height=imp.getHeight();
        size=width*height;
        type=imp.getType();
        return DOES_ALL-DOES_RGB;
    }

    @Override
    public void run(ImageProcessor ip) 
    {
        int t,b,k;
        ImageStack BL_stack=new ImageStack(width,height);
        ImageStack DL_stack=new ImageStack(width,height);
        float[][] WB=new float[nSlices][size];
        float[][] WD=new float[nSlices][size];
        float[] pixelsWHT=new float[size];
        float[] WD_0= new float[size] ;
        float[] WB_0=new float[size] ;
        float sum_wb,sum_wd;
        float eps=1e-12f;
        for (k=0;k<nSlices;k++)
        {
            switch(type)//Convert images to float
            {
                case 0://GRAY8
                    byte[]pixelsB=(byte[])stack.getProcessor(k+1).getPixels();
                    for(a=0;a<size;a++)
                        pixelsWHT[a]=(float)(pixelsB[a]&0xff);
                    break;
                case 1://GRAY16
                    short[]pixelsS=(short[])stack.getProcessor(k+1).getPixels();
                    for(a=0;a<size;a++)
                        pixelsWHT[a]=(float)pixelsS[a];
                    break;
                case 2://GRAY32
                    float[] pixelsF= (float[])stack.getProcessor(k+1).getPixels();//(float[])ip.getPixels();
                    for(a=0;a<size;a++)
                        pixelsWHT[a]=pixelsF[a];
                    break;
            }
    //        for i in range(N):
    //            WB[:,:,:,i],_=EPF(I[:,:,:,i],G[:,:,:,i],Sg1,Sc1)
    //            WD[:,:,:,i],_=EPF(I[:,:,:,i],G[:,:,:,i],Sg2,Sc2)
            EPF(WB_0,pixelsWHT,pixelsWHT,sigma1,scale1);
            /*for(y=0;y<size;y++)
                WB[k][y]=WB_0[y];*/
            System.arraycopy(WB_0,0,WB[k],0,size);
            EPF(WD_0,pixelsWHT,pixelsWHT,sigma2,scale2);
            /*for(y=0;y<size;y++)
                WD[k][y]=WD_0[y];*/
            System.arraycopy(WD_0,0,WD[k],0,size);
//        # Rolling Filter: WB and WD are now the inputs and WHTs are acting as guidance images,
//          which means guidance image is same and the input is changing in each iteration.

//        for t in range(iter1-1):
//          for i in range(N):
//            WB[:,:,:,i],_=EPF(WB[:,:,:,i],G[:,:,:,i],Sg1,Sc1)
//            WD[:,:,:,i],_=EPF(WD[:,:,:,i],G[:,:,:,i],Sg2,Sc2)
//            print(f'Iteration {t+1}')
            for(t=1;t<iter;t++)
            {
                EPF(WB[k],WB[k],pixelsWHT,sigma1,scale1);
                EPF(WD[k],WD[k],pixelsWHT,sigma2,scale2);
            }
        }
        for(y=0;y< size;y++)
        {
//            # Base Layer Weight Normalization
//            WB_start=np.zeros((r,c,ch))
//            for i in range(N):
//                WT=WB[:,:,:,i]
//                WB_start += WT
//                Wsum=WB_start
//
//            for i in range(N):
//                WB[:,:,:,i]=WB[:,:,:,i] / (Wsum + 1e-12)
            sum_wb=0;
            for (k=0;k<nSlices;k++)
                sum_wb+=WB[k][y];
            for (k=0;k<nSlices;k++)
                WB[k][y]/=(sum_wb+eps);
//            # Detail Layer Weight Normalization
//            WD_start=np.zeros((r,c,ch))
//            for i in range(N):
//                WDT=WD[:,:,:,i]
//                WD_start += WDT
//                WDsum=WD_start
//
//            for i in range(N):
//                WD[:,:,:,i]=WD[:,:,:,i] / (WDsum + 1e-12)
            sum_wd=0;
            for (k=0;k<nSlices;k++)
                sum_wd+=WD[k][y];
            for (k=0;k<nSlices;k++)
               WD[k][y]/=(sum_wd+eps);
        }
        for (k=0;k<nSlices;k++)
        {
        // Add each BL and DL output for each image into the stack
            ImagePlus impBL32=NewImage.createFloatImage(null,width,height,1,NewImage.FILL_BLACK);
            ImagePlus impDL32=NewImage.createFloatImage(null,width,height,1,NewImage.FILL_BLACK);
            ImageProcessor ipBL32=impBL32.getProcessor();
            ImageProcessor ipDL32=impDL32.getProcessor();
            float[] bl_pixels32=(float[])ipBL32.getPixels();
            float[] dl_pixels32=(float[])ipDL32.getPixels();
            for(y=0;y<size;y++)
            {
                bl_pixels32[y]=(float)WB[k][y];
                dl_pixels32[y]=(float)WD[k][y];
            }
            BL_stack.addSlice("",ipBL32);
            DL_stack.addSlice("",ipDL32);
        }
        // Create two new stack windows for BL and DL outputs and show
        ImagePlus impBL=new ImagePlus("WB",BL_stack);
        IJ.resetMinAndMax(impBL);
        impBL.show();
        ImagePlus impDL=new ImagePlus("WD",DL_stack);
        IJ.resetMinAndMax(impDL);
        impDL.show();
        }

   public void EPF(float[] J,float[] imageG,float[] imageI,float sigmaS,float scale)
   {
        // Inputs: J -- output of the function
        //    imageG -- guidance image G 
        //    imageI -- image I to be processed
        //    SigmaS -- bilateral spacial parameter,if SigmaS is large then larger scale objects are smoothed out.
        //     scale -- bilateral range parameter,if the scale<1 then sharper results are obtained.
        // width,height -- size of the G and I
        
        // Calculate the patch size
        patchSize=(int)(4*sigmaS+1);
        if (patchSize%2==0)
            patchSize++;
        // Calculate the size of the padding and the img size with padding
        vc=patchSize/2;
        size=width*height;
        bigWidth=width+patchSize-1;
        bigHeight=height+patchSize-1;
        //----------------------------------------------------------------------
        // GAUSSIAN,gets kernel
 /*       String aux="";
        for (i=1;i<100;i++)
        {
            double gauss=Math.exp(-(i*i)/(2.*sigma1*sigma1));
            if (gauss>0.003)
                aux+=gauss+",";
            else
                break;
        }
        ha_kernel=new float[aux.split(",").length*2+1];
        kw=ha_kernel.length;
        gvc=kw/2;
        gscale=ha_kernel[gvc]=1f;//Central weight of the vector
        String[] weights=aux.split(",");
        for (a=0,i=gvc-1;i>=0;i--) 
        {
            ha_kernel[a]=Float.parseFloat(weights[i]);
            gscale+=ha_kernel[a];
            ha_kernel[kw-1-a]=Float.parseFloat(weights[i]);
            gscale+=ha_kernel[a++];
        }*/
           
        
        //----------------------------------------------------------------------
        // GAUSSIAN,gets kernel
        kw=(int)(6*sigmaS+1);
        gvc=kw/2;
        ha_kernel=new float[kw];  
        int centro=kw/2;
        //----------------------------------------------------------------------
        // Mascara gausiana y normalización
        for (i = 0;i<kw;i++)
        {
            float distanciaLado=i-centro;
            ha_kernel[i]=(float)Math.exp(-(distanciaLado*distanciaLado)/(2*sigmaS*sigmaS));
            gscale+=ha_kernel[i];
        }
        //----------------------------------------------------------------------
        
       
        bigWidth_g=width+kw-1;
        bigHeight_g=height+kw-1;
        //----------------------------------------------------------------------
        // muG=uniform_filter(G,size=patchSize,mode=padMethod)
        // PixelsG -> guidance image G,padded 
        float[][]pixelsG=new float[bigHeight][bigWidth];
        //Copy the pixels in the middle of the enlarged image
        for(a=y=0;y<height;y++)
            for(x=0;x<width;x++)
                pixelsG[y+vc][x+vc]=imageG[a++];
        //Mirroring - padding with the mirrored pixels (padMode=reflect)
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
        //Convolve - Uniform Filter
        float[][] pixelsG2=new float[bigHeight][bigWidth];
        float[][] pixelsG_=new float[bigHeight][bigWidth];
        for(y=0;y<bigHeight;y++)//Convolve in the x axis
            for(x=vc;x<bigWidth-vc;x++)
            {
                for(sum=0,v=-vc;v<=vc;v++)
                    sum+=pixelsG[y][x+v];
                pixelsG2[y][x]=sum/patchSize;
            }
        for(x=0;x<bigWidth;x++)//Convolve in the y axis
            for(y=vc;y<bigHeight-vc;y++)
            {
                for(sum=0,v=-vc;v<=vc;v++)
                    sum += pixelsG2[y+v][x];
                pixelsG_[y][x]=sum/patchSize;
            }
        //----------------------------------------------------------------------
        //muGG=uniform_filter(G * G,size=patchSize,mode=padMethod) # patch mean of G
        // Create G * G 
        float[][]pixelsGG=new float[bigHeight][bigWidth];
        //Copy the pixels in the middle of the enlarged image
        for(a=y=0;y<height;y++)
            for(x=0;x<width;x++)
                pixelsGG[y+vc][x+vc]=imageG[a]*imageG[a++];
        //Mirroring - padding with the mirrored pixels (padMode=reflect)
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
        //Convolve - Uniform Filter
        float[][] pixelsGG2=new float[bigHeight][bigWidth];
        float[][] pixelsGG_=new float[bigHeight][bigWidth];
        for(y=0;y<bigHeight;y++)//Convolve in the x axis
            for(x=vc;x<bigWidth-vc;x++)
            {
                for(sum=0,v=-vc;v<=vc;v++)
                    sum+=pixelsGG[y][x+v];
                pixelsGG2[y][x]=sum/patchSize;
            }
        for(x=0;x<bigWidth;x++)//Convolve in the y axis
            for(y=vc;y<bigHeight-vc;y++)
            {
                for(sum=0,v=-vc;v<=vc;v++)
                    sum+=pixelsGG2[y+v][x];
                pixelsGG_[y][x]=sum/patchSize;
            }
        //----------------------------------------------------------------------
        // w=np.maximum(0,muGG - muG * muG)
        float[][] w=new float[bigHeight][bigWidth];
        for(y=0;y<bigHeight;y++)
            for(x=0;x<bigWidth;x++)
            {
                w[y][x]=pixelsGG_[y][x]-(pixelsG_[y][x]*pixelsG_[y][x]);
                if (w[y][x] < 0)
                    w[y][x]=0;
            }
        // SigmaR=scale * np.mean(w)
        float w_mean=0.f;
        for(y=vc;y<bigHeight-vc;y++)
            for(x=vc;x<bigWidth-vc;x++)
                w_mean+=w[y][x];
        w_mean/=size;
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
        //Mirroring - padding with the mirrored pixels
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
        float[] pixels2= new float[bigHeight_g*bigWidth_g];
        float[] normalizeFactor= new float[height*width];
        for(y=gvc;y<bigHeight_g-gvc;y++)//Convolve in the x axis
        {
            offset=y*bigWidth_g;
            for(x=gvc;x<bigWidth_g-gvc;x++)
            {
                a=offset+x;
                for(b=0,sum=0,v=-gvc;v<=gvc;v++)
                    sum+=copy[a+v]*(ha_kernel[b++]);
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
                weighted_I[y+gvc][x+gvc]=imageI[a++] * w_[y][x];
        // Uniform filter on w * I
        //Mirroring - padding with the mirrored pixels
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
        float[] pixels3= new float[bigHeight_g*bigWidth_g];
        for(y=gvc;y<bigHeight_g-gvc;y++)//Convolve in the x axis
        {
            offset=y*bigWidth_g;
            for(x=gvc;x<bigWidth_g-gvc;x++)
            {
                a=offset+x;
                for(b=0,sum=0,v=-gvc;v<=gvc;v++)
                    sum+= (copy_wI[a+v]) * (ha_kernel[b++]);
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
        // Divide the filtered w * I by epsilon and then add normalization factor
        for(a=0;a< size;a++)
            J[a]/=(epsilon+normalizeFactor[a]);
   }
}