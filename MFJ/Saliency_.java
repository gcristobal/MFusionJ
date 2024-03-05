import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.plugin.filter.*;

public class Saliency_ implements PlugInFilter
{
    ImagePlus imp;
    int bigHeight,bigHeight_g,height,bigWidth,bigWidth_g,width;
    int nSlices,size,type,kw,vc,vc2;;
    float scale,sigma=1f;
    float[] kernel;
    ImageStack stack;
    ImageStack SAL_stack=new ImageStack(width,height);

    @Override
    public int setup(String arg,ImagePlus imp) 
    {
        int a,i;
        this.imp=imp;
        GenericDialog gd=new GenericDialog("Gaussian filter");
        gd.addNumericField("Sigma:",sigma,1);
        sigma=(float)gd.getNextNumber();
        width=imp.getWidth();
        height=imp.getHeight();
        size=width*height;
        bigWidth=width+2;
        bigHeight=height+2;
        type=imp.getType();
        stack=imp.getStack();
        nSlices=imp.getStackSize();
        //----------------------------------------------------------------------
        // GAUSSIAN,gets kernel
        kw=(int)(6*sigma+1);
        vc=kw/2;
        kernel=new float[kw];  
        int centro=kw/2;
        //----------------------------------------------------------------------
        // Mascara gausiana y normalización
        for (i = 0;i<kw;i++)
        {
            float distanciaLado=i-centro;
            kernel[i]=(float)Math.exp(-(distanciaLado*distanciaLado)/(2*sigma*sigma));
            scale+=kernel[i];
        }
        //------------------------------------------------------------------
        bigWidth_g=width+kw-1;
        bigHeight_g=height+kw-1;
        type=imp.getType();
        return DOES_ALL-DOES_RGB;
    }

    @Override
    public void run(ImageProcessor ip)
    {
        int k,i,p,j,a,v,x,y,xx,yy,l,offset,b;
        float sum;
        
        for (k=0;k<nSlices;k++)
        {
            float[] pixelsR=new float[size];
            switch(type)//Convert images to float
            {
                case 0://GRAY8
                    byte[]pixelsB=(byte[])stack.getProcessor(k+1).getPixels();
                    for(a=0;a<size;a++)
                        pixelsR[a]=(float)(pixelsB[a]&0xff);
                    break;
                case 1://GRAY16
                    short[]pixelsS=(short[])stack.getProcessor(k+1).getPixels();
                    for(a=0;a<size;a++)
                        pixelsR[a]=(float)pixelsS[a];
                    break;
                case 2://GRAY32
                    float[]pixelsF=(float[])stack.getProcessor(k+1).getPixels();
                    for(a=0;a<size;a++)
                        pixelsR[a]=pixelsF[a];
                    break;
            }
            //----------------------------------------------------------------------
            //Charge the original image
            float[][]pixelsM=new float[bigHeight][bigWidth];
            //Copy the pixels in the middle of the enlarged image
            for(a=y=0;y<height;y++)
                for(x=0;x<width;x++)
                    pixelsM[y+1][x+1]=pixelsR[a++];
            //----------------------------------------------------------------------
            //Mirroring - padding with the mirrored pixels (differs from the Python code!)
            //Duplicate the pixels of the left and right parts of the image 
            for(i=1;i<bigHeight-1;i++)
            {
                for(j=1,p=0;p<1;p++)
                    pixelsM[i][p]=pixelsM[i][j--];
                j=(bigWidth-2);
                for(p=bigWidth-1;p>bigWidth-1-1;p--)
                    pixelsM[i][p]=pixelsM[i][j++];
            }
            //----------------------------------------------------------------------
            //Duplicate the pixels of the top and down sides of the image
            for(i=0;i<bigWidth;i++)
            {
                for(j=1,p=0;p<1;p++)
                    pixelsM[p][i]=pixelsM[j--][i];
                j=(bigHeight-2*1);
                for(p=bigHeight-1;p>bigHeight-2;p--)
                    pixelsM[p][i]=pixelsM[j++][i];
            }
            //----------------------------------------------------------------------
            // Apply the mask [0 -1 0 -1 4 -1 0 -1 0]
            float[][] pixelsMX=new float[bigHeight][bigWidth];
            float[][] pixelsMY=new float[bigHeight][bigWidth];
            float[] mask=new float[]{0,-1,0,-1,4,-1,0,-1,0};
            for(y=1;y<bigHeight-1;y++)
                for(x=1;x<bigWidth-1;x++)
                {
                    for(l=0,sum=0,yy=-1;yy<=1;yy++)
                        for(xx=-1;xx<=1;xx++)
                            sum+=pixelsM[y+yy][x+xx]*mask[l++];
                    pixelsMX[y][x]=sum;
                    pixelsMY[y][x]=sum;
                }
            // Apply the mask [0 -1 0 -1 4 -1 0 -1 0] again
/*            for(y=1;y<bigHeight-1;y++)
                for(x=1;x<bigWidth-1;x++)
                {
                    for(l=0,sum=0,yy=-1;yy<=1;yy++)
                        for(xx=-1;xx<=1;xx++)
                            sum+=pixelsM[y+yy][x+xx]*mask[l++];
                    pixelsMY[y][x]=sum;
                }*/
            // WW=np.sqrt(Ix1 ** 2 + Iy1 ** 2)
            float[] pixelsWW=new float[size];
            for(a=0,y=1;y<bigHeight-1;y++)
                for(x=1;x<bigWidth-1;x++)
//                    pixelsWW[a++]=(float)Math.sqrt((pixelsMX[y][x]*pixelsMX[y][x])+(pixelsMY[y][x]*pixelsMY[y][x]));
                    pixelsWW[a++]=(float)Math.sqrt(2*pixelsMX[y][x]*pixelsMX[y][x]);
            // GAUSSIAN BLUR
            //Charge the original image
            float[][]pixelsM2=new float[bigHeight_g][bigWidth_g];
            //Copy the pixels in the middle of the enlarged image
            for(a=y=0;y<height;y++)
                for(x=0;x<width;x++)
                    pixelsM2[y+vc][x+vc]=pixelsWW[a++];
            // Mirroring
            //Duplicate the pixels of the left and right parts of the image 
            for(i=1;i<bigHeight_g-1;i++)
            {
                for(j=1,p=0;p<1;p++)
                    pixelsM2[i][p]=pixelsM2[i][j--];
                j=(bigWidth_g-2);
                for(p=bigWidth_g-1;p>bigWidth_g-1-1;p--)
                    pixelsM2[i][p]=pixelsM2[i][j++];
            }
            //----------------------------------------------------------------------
            //Duplicate the pixels of the left and right sides of the image
            for(i=0;i<bigWidth_g;i++)
            {
                for(j=1,p=0;p<1;p++)
                    pixelsM2[p][i]=pixelsM2[j--][i];
                j=(bigHeight_g-2*1);
                for(p=bigHeight_g-1;p>bigHeight_g-2;p--)
                    pixelsM2[p][i]=pixelsM2[j++][i];
            } 
            float[] copy=new float[bigHeight_g*bigWidth_g];
            for(a=y=0;y<bigHeight_g;y++)
                for(x=0;x<bigWidth_g;x++)
                    copy[a++]=pixelsM2[y][x];
            // Convolve Gaussian
            float[] pixels2= new float[bigHeight_g*bigWidth_g];
            for(y=vc;y<bigHeight_g-vc;y++)//Convolve in the x axis
            {
                offset=y*bigWidth_g;
                for(x=vc;x<bigWidth_g-vc;x++)
                {
                    a=offset+x;
                    for(b=0,sum=0,v=-vc;v<=vc;v++)
                        sum+=copy[a+v]*(kernel[b++]);
                    pixels2[a]=sum/scale;
                }
            }
            for(y=vc;y<bigHeight_g-vc;y++)//Convolve in the y axis
            {
                offset=y*bigWidth_g;
                for(x=vc;x<bigWidth_g-vc;x++)
                {
                    a=offset+x;
                    for(b=0,sum=0,v=-vc;v<=vc;v++)
                        sum+=pixels2[a+v*bigWidth_g]*kernel[b++];
                    pixelsR[(y-vc)*width+x-vc]=sum/scale;
                }
            }
            switch(type)//Convert images to the original type
            {
                case 0://GRAY8
                    byte[]pixelsB=(byte[])stack.getProcessor(k+1).getPixels();
                    for (a=0;a<size;a++)
                        pixelsB[a]=(byte)(pixelsR[a]+.5);
                    break;
                case 1://GRAY16
                    short[]pixelsS=(short[])stack.getProcessor(k+1).getPixels();
                    for (a=0;a<size;a++)
                        pixelsS[a]=(short)(pixelsR[a]+.5);
                    break;
                case 2://GRAY32
                    float[]pixelsF=(float[])stack.getProcessor(k+1).getPixels();
                    for (a=0;a<size;a++)
                        pixelsF[a]=pixelsR[a];
                    break;
            }
        }
        IJ.resetMinAndMax(imp);
        imp.updateAndDraw();
        imp.show();
    }
}