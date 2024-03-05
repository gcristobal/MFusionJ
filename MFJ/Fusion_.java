import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.plugin.*;

public class Fusion_ implements PlugIn
{
    public void run(String arg)
    {
        float sum_bf,sum_df;
        int i,k;
        if (arg.equals("about"))
        {
            showAbout();
            return;
        }
        //----------------------------------------------------------------------
        //Get BL,DL,WB,WD
        int[] wList=WindowManager.getIDList();
        if (wList==null||wList.length<4)
        {
            IJ.showMessage("Fusion inputs","This plugin requires 4 stacks of the same size.");
            return;
        }
        String[] titles=new String[wList.length];
        for (i=0;i<wList.length;i++)
        {
            ImagePlus imp=WindowManager.getImage(wList[i]);
            titles[i]=imp!=null?imp.getTitle():"";
        }
        GenericDialog gd=new GenericDialog("Fusion Inputs");
        gd.addChoice("Base Layer:",titles,titles[0]);
        gd.addChoice("Detail Layer:",titles,titles[1]);
        gd.addChoice("WB:",titles,titles[2]);
        gd.addChoice("WD:",titles,titles[3]); 
        gd.showDialog();
        if (gd.wasCanceled())
            return;
        int index_bl=gd.getNextChoiceIndex();
        ImagePlus   impBL=WindowManager.getImage(wList[index_bl]);//BL image
        int index_dl=gd.getNextChoiceIndex();
        ImagePlus   impDL=WindowManager.getImage(wList[index_dl]);//DL image
        int index_wb=gd.getNextChoiceIndex();
        ImagePlus   impWB=WindowManager.getImage(wList[index_wb]);//Ambient image
        int index_wd=gd.getNextChoiceIndex();
        ImagePlus   impWD=WindowManager.getImage(wList[index_wd]);//Flash image
        if (impBL.getType()!=ImagePlus.GRAY32||impDL.getType()!=ImagePlus.GRAY32||impWB.getType()!=ImagePlus.GRAY32||impWD.getType()!=ImagePlus.GRAY32)
        {
            IJ.error("Float grayscale images required");
            return;
        }
        int width=impBL.getWidth();
        int height=impBL.getHeight();
        int size=width*height;
        int nSlices=impBL.getStackSize();
        int nSlices_DL=impDL.getStackSize();
        int nSlices_WB=impWB.getStackSize();
        int nSlices_WD=impWD.getStackSize();
        ImageStack stackBL=impBL.getStack();
        ImageStack stackDL=impDL.getStack();
        ImageStack stackWB=impWB.getStack();
        ImageStack stackWD=impWD.getStack();
        ImageStack Fusion=new ImageStack(width,height);
        if (nSlices!=nSlices_DL||nSlices_WB!=nSlices_WD||nSlices!=nSlices_WB)
        {
            IJ.error("All stacks must have same number of slices!");
            return;
        }
        float[][] BF0=new float[nSlices][size];
        float[][] DF0=new float[nSlices][size];
        for(k=0;k<nSlices;k++)
        {
            float[] BL=(float[])stackBL.getProcessor(k+1).getPixels();
            float[] DL=(float[])stackDL.getProcessor(k+1).getPixels();
            float[] WB=(float[])stackWB.getProcessor(k+1).getPixels();
            float[] WD=(float[])stackWD.getProcessor(k+1).getPixels();
            // Multiply each pixel in each image with the corresponding weight (for BL and DL)
            for(i=0;i<size;i++)
            {
                BF0[k][i]=BL[i]*WB[i];
                DF0[k][i]=DL[i]*WD[i];
            }
        }
        ImagePlus imp=NewImage.createFloatImage(null,width,height,1,NewImage.FILL_BLACK);
        ImageProcessor ip=imp.getProcessor();
        float[] pixels=(float[])ip.getPixels();
        for(i=0;i<size;i++)
        {
            sum_bf=0;
            sum_df=0;
            // Fusion
            // For each pixels,sum up the values coming from each image/slice
            for(k=0;k<nSlices;k++)
            {
                sum_bf+=BF0[k][i];
                sum_df+=DF0[k][i];
            }
            // Combine BF[i] and DF[i]
            pixels[i]=sum_bf+sum_df;
        }
        Fusion.addSlice("",ip);
        // Create two new stack windows for BL and DL outputs and show
        IJ.showProgress(1.0);
        ImagePlus impF=new ImagePlus("Fused Image",Fusion);
        impF.show();
        IJ.setMinAndMax(0,255);
    }

    void showAbout()
    {
        IJ.showMessage("Fusion","This plug-in creates the fused image. "
        +"It requires Base and Detail Layers (output of the plugin Decompose),"
        +"as well as WB and WD. WB and WD can be optained via Saliency -> Weight -> WMAP.");
    }
}