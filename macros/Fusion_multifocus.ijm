macro "Fusion_multifocus"
{
values=getArgument();
if (lengthOf(values)==0)
{
    Dialog.create("Fusion");
    Dialog.addNumber("Decompose Iterations:",1);
    Dialog.addNumber("Decompose Gamma:",1); // 1.2
    Dialog.addNumber("Sigma1:",8);
    Dialog.addNumber("Scale1:",1);
    Dialog.addNumber("Sigma2:",4);
    Dialog.addNumber("Scale2:",0.5);
    Dialog.addNumber("WMAP Iterations:",3);
    Dialog.show();
    decomp_iter=Dialog.getNumber();
    gamma=Dialog.getNumber();
    sigma1=Dialog.getNumber();
    scale1=Dialog.getNumber();
    sigma2=Dialog.getNumber();
    scale2=Dialog.getNumber();
    wmap_iter=Dialog.getNumber();
}
else
{
    a=split(values,"");
    decomp_iter=a[0];
    gamma=a[1];
    sigma1=a[2];
    scale1=a[3];
    sigma2=a[4];
    scale2=a[5];
    wmap_iter=a[6];
}
title=getTitle();
selectWindow(title);
// check if the stack is 8-bit or RGB
if ( bitDepth() == 24 ) {
     run("Split Channels");
     selectWindow(title +" (red)");
// FusionMacro -----------------
      run("Decompose ","sigmas=1 scale=0.5 iterations="+decomp_iter+" gamma="+ gamma+"");
      selectWindow(title +" (red)");
      run("Saliency ");
      selectWindow(title +" (red)");
      run("Weight ");
      selectWindow(title +" (red)");
      run("WMAP ", "sigma="+sigma1+" scale="+scale1+" sigma_0="+sigma2+" scale_0="+scale2+" iterations="+wmap_iter);
      selectWindow(title +" (red)");
      close();

      run("Fusion ", "base=[Base Layer] detail=[Detail Layer] wb=WD wd=WB");
      selectWindow("Base Layer");
      close();
      selectWindow("Detail Layer");
     close();
      selectWindow("WB");
      close();
      selectWindow("WD");
     close();
      selectWindow("Fused Image");
      rename("Fused Image Result of " + title +" (red)");
// -----------------
      selectWindow(title +" (green)");
// FusionMacro -----------------
      run("Decompose ", "sigmas=1 scale=0.5 iterations="+decomp_iter+" gamma="+ gamma+"");

      selectWindow(title +" (green)");
      run("Saliency ");
      selectWindow(title +" (green)");
      run("Weight ");
      selectWindow(title +" (green)");
      run("WMAP ", "sigma="+sigma1+" scale="+scale1+" sigma_0="+sigma2+" scale_0="+scale2+" iterations="+wmap_iter);
      selectWindow(title +" (green)");
      close();
      run("Fusion ", "base=[Base Layer] detail=[Detail Layer] wb=WD wd=WB");
      selectWindow("Base Layer");
     close();
     selectWindow("Detail Layer");
     close();
     selectWindow("WB");
     close();
     selectWindow("WD");
     close();

      selectWindow("Fused Image");
      rename("Fused Image Result of " + title +" (green)");
// -----------------

     selectWindow(title + " (blue)");
// FusionMacro -----------------
     run("Decompose ", "sigmas=1 scale=0.5 iterations="+decomp_iter+" gamma="+ gamma+"");

     selectWindow(title + " (blue)");
     run("Saliency ");
      selectWindow(title + " (blue)");
      run("Weight ");
      selectWindow(title + " (blue)");
      run("WMAP ", "sigma="+sigma1+" scale="+scale1+" sigma_0="+sigma2+" scale_0="+scale2+" iterations="+wmap_iter);
      selectWindow(title + " (blue)");
     close();

      run("Fusion ", "base=[Base Layer] detail=[Detail Layer] wb=WD wd=WB");
      selectWindow("Base Layer");
     close();
      selectWindow("Detail Layer");
     close();
     selectWindow("WB");
     close();
     selectWindow("WD");
      close();
      
     selectWindow("Fused Image");
     rename("Fused Image Result of " + title +" (blue)");

      run("Merge Channels...", "c1=[Fused Image Result of " + title +" (red)] c2=[Fused Image Result of " + title +" (green)] c3=[Fused Image Result of " + title +" (blue)]");
      selectWindow("RGB");
      rename("Fused RGB Image Result of " + title );
    }
    
    // process 8-bit stacks
    else {
        
        // FusionMacro -----------------
        run("Decompose ","sigmas=1 scale=0.5 iterations="+decomp_iter+" gamma="+ gamma+"");
        selectWindow(title);
        //selectWindow(title +" (red)");
        run("Saliency ");
        selectWindow(title);
       //selectWindow(title +" (red)");
       run("Weight ");
       //selectWindow(title +" (red)");
       selectWindow(title);
       run("WMAP ", "sigma="+sigma1+" scale="+scale1+" sigma_0="+sigma2+" scale_0="+scale2+" iterations="+wmap_iter);
       //selectWindow(title +" (red)");
       selectWindow(title);
      close();

       run("Fusion ", "base=[Base Layer] detail=[Detail Layer] wb=WD wd=WB");
       selectWindow("Base Layer");
      close();
      selectWindow("Detail Layer");
      close();
      selectWindow("WB");
      close();
      selectWindow("WD");
     close();
      selectWindow("Fused Image");
       //rename("Fused Image Result of " + title +" (red)");
       rename("Fused Image Result of " + title);
   }
        
