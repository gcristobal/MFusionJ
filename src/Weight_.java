import ij.*;
import ij.process.*;
import ij.plugin.filter.*;

public class Weight_ implements PlugInFilter
{
    ImagePlus imp;
    int nSlices,type;
    ImageStack stack;

    @Override
    public int setup(String arg,ImagePlus imp) 
    {
        this.imp=imp;
        stack=imp.getStack();
        nSlices=imp.getStackSize();     
        type=imp.getType();
        return DOES_ALL-DOES_RGB;
    }

    @Override
    public void run(ImageProcessor ip)
    {
        int a,k;
        float max;
        int height=ip.getHeight();
        int width=ip.getWidth();
        int size=height*width;
        ImageStack BL_stack=new ImageStack(width,height);

        float[][] pixelsR=new float[nSlices][size];// array of pixel values of all the images on the stack
        for (k=0;k<nSlices;k++)
            switch(type)//Convert images to float
            {
                case 0://GRAY8
                    byte[]pixelsB=(byte[])stack.getProcessor(k+1).getPixels();
                    for(a=0;a<size;a++)
                        pixelsR[k][a]=(float)(pixelsB[a]&0xff);
                    break;
                case 1://GRAY16
                    short[]pixelsS=(short[])stack.getProcessor(k+1).getPixels();
                    for(a=0;a<size;a++)
                        pixelsR[k][a]=(float)pixelsS[a];
                    break;
                case 2://GRAY32
                    float[] pixelsF=(float[])stack.getProcessor(k+1).getPixels();
                    for(a=0;a<size;a++)
                        pixelsR[k][a]=pixelsF[a];
                    break;
            }
        switch(type)//Convert images to the original type
        {
            case 0://GRAY8
                for (a=0;a<size;a++)
                {
                    max=pixelsR[0][a];
                    for (k=1;k<nSlices;k++)
                        if (max<pixelsR[k][a])
                            max=pixelsR[k][a];
                    for (k=0;k<nSlices;k++)
                    {
                        byte[]pixelsB=(byte[])stack.getProcessor(k+1).getPixels();
                        pixelsB[a]=(byte)(pixelsR[k][a]>=max?1:0);
                    }
                }
                break;
            case 1://GRAY16
                for (a=0;a<size;a++)
                {
                    max=pixelsR[0][a];
                    for (k=1;k<nSlices;k++)
                        if (max<pixelsR[k][a])
                            max=pixelsR[k][a];
                    for (k=0;k<nSlices;k++)
                    {
                        short[]pixelsS=(short[])stack.getProcessor(k+1).getPixels();
                        pixelsS[a]=(short)(pixelsR[k][a]>=max?1:0);
                    }
                }
                break;
            case 2://GRAY32
                for (a=0;a<size;a++)
                {
                    max=pixelsR[0][a];
                    for (k=1;k<nSlices;k++)
                        if (max<pixelsR[k][a])
                            max=pixelsR[k][a];
                    for (k=0;k<nSlices;k++)
                    {
                        float[]pixelsF=(float[])stack.getProcessor(k+1).getPixels();
                        pixelsF[a]=pixelsR[k][a]>=max?1:0;
                    }
                }
                break;
        }
        IJ.resetMinAndMax(imp);
        imp.show();
    }
}

