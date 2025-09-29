macro "Fusion_multifocus"
{
// Input parameters
values=getArgument();
if (lengthOf(values)==0)
    {
        Dialog.create("Fusion");
        Dialog.addNumber("Decompose Iterations:",1);
        Dialog.addNumber("Decompose Gamma:",1);
        Dialog.addNumber("Sigma1:",2);
        Dialog.addNumber("Scale1:",1);
        Dialog.addNumber("Sigma2:",1);
        Dialog.addNumber("Scale2:",0.5);
        Dialog.addNumber("WMAP Iterations:",1);
        input_types = newArray("Folder", "Stack file");
        Dialog.addChoice("Input format",input_types);
        Dialog.show();
        decomp_iter=Dialog.getNumber();
        gamma=Dialog.getNumber();
        sigma1=Dialog.getNumber();
        scale1=Dialog.getNumber();
        sigma2=Dialog.getNumber();
        scale2=Dialog.getNumber();
        wmap_iter=Dialog.getNumber();
        input_format=Dialog.getChoice();
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
        input_format=a[7];
    }

// Run in batch mode
setBatchMode(true); 
// Open the folder with the images to be fused or the stack file
if (input_format=="Stack file")
{
    input_path=File.openDialog("Select the stack file");
    print("Loading stack file: "+input_path);
    open(input_path);
}
else
{
    input_path = getDirectory("Select the folder with the images to be fused or the stack file");
    //Check if selected path is a folder
    if (File.isDirectory(input_path) == 1) {   
            print("Loading images from folder: "+input_path);
            list = getFileList(input_path);
            for (i=0; i<list.length; i++)
            {
                open(input_path+list[i]);
            }
            // add to stack
            run("Images to Stack", "name=Stack title=[] use");
        }
}
title=getTitle();
selectWindow(title);
// Init progress bar
progress = 0;
showProgress(progress);
showStatus("Fusion in progress...");
print("Fusion in progress...");
// check if the stack is 8-bit or RGB
if ( bitDepth() == 24 ) {
    run("Split Channels");
    selectWindow(title +" (red)");
// FusionMacro -----------------
    run("Decompose ","sigmas=1 scale=0.5 iterations="+decomp_iter+" gamma="+ gamma+"");
    progress = progress + 0.066;
    showProgress(progress);
    showStatus("Fusion in progress...");
    selectWindow(title +" (red)");
    run("Saliency ");
    progress = progress + 0.066;
    showProgress(progress);
    showStatus("Fusion in progress...");
    selectWindow(title +" (red)");
    run("Weight ");
    progress = progress + 0.066;
    showProgress(progress);
    showStatus("Fusion in progress...");
    selectWindow(title +" (red)");
    run("WMAP ", "sigma="+sigma1+" scale="+scale1+" sigma_0="+sigma2+" scale_0="+scale2+" iterations="+wmap_iter);
    progress = progress + 0.066;
    showProgress(progress);
    showStatus("Fusion in progress...");
    selectWindow(title +" (red)");
    close();
    run("Fusion ", "base=[Base Layer] detail=[Detail Layer] wb=WB wd=WD");
    progress = progress + 0.066;
    showProgress(progress);
    showStatus("Fusion in progress...");
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
    progress = progress + 0.066;
    showProgress(progress);
    showStatus("Fusion in progress...");
    selectWindow(title +" (green)");
    run("Saliency ");
    progress = progress + 0.066;
    showProgress(progress);
    showStatus("Fusion in progress...");
    selectWindow(title +" (green)");
    run("Weight ");
    progress = progress + 0.066;
    showProgress(progress);
    showStatus("Fusion in progress...");
    selectWindow(title +" (green)");
    run("WMAP ", "sigma="+sigma1+" scale="+scale1+" sigma_0="+sigma2+" scale_0="+scale2+" iterations="+wmap_iter);
    progress = progress + 0.066;
    showProgress(progress);
    showStatus("Fusion in progress...");
    selectWindow(title +" (green)");
    close();
    run("Fusion ", "base=[Base Layer] detail=[Detail Layer] wb=WB wd=WD");
    progress = progress + 0.066;
    showProgress(progress);
    showStatus("Fusion in progress...");
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
    progress = progress + 0.066;
    showProgress(progress);
    showStatus("Fusion in progress...");
    selectWindow(title + " (blue)");
    run("Saliency ");
    progress = progress + 0.066;
    showProgress(progress);
    showStatus("Fusion in progress...");
    selectWindow(title + " (blue)");
    run("Weight ");
    progress = progress + 0.066;
    showProgress(progress);
    showStatus("Fusion in progress...");
    selectWindow(title + " (blue)");
    run("WMAP ", "sigma="+sigma1+" scale="+scale1+" sigma_0="+sigma2+" scale_0="+scale2+" iterations="+wmap_iter);
    progress = progress + 0.066;
    showProgress(progress);
    showStatus("Fusion in progress...");
    selectWindow(title + " (blue)");
    close();
    run("Fusion ", "base=[Base Layer] detail=[Detail Layer] wb=WB wd=WD");
    progress = progress + 0.066;
    showProgress(progress);
    showStatus("Fusion in progress...");
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
    //Save the fused image
    if (input_format=="Stack file")
    {
        fused_image_name = File.getName(input_path);
        fused_image_name = substring(fused_image_name, 0, fused_image_name.length - 4) + "_Fused.tif";
        saveAs("Tiff", File.getParent(input_path) + File.separator + fused_image_name);

    }
    else
    {
        // Extract folder name from input_path
        folder_path = substring(input_path, 0, input_path.length - 1); // Remove trailing separator
        folder_name = substring(folder_path, lastIndexOf(folder_path, File.separator)+1, folder_path.length);
        fused_image_name = folder_name + "_Fused.tif";
        saveAs("Tiff", input_path + fused_image_name);
    }
    print("Fusion completed");
    // Update progress bar
    showProgress(1);
    }
    
    // process 8-bit stacks
    else {
        if (bitDepth() != 8) {
            print("Converting stack to 8-bit...");
            run("8-bit");
        }
        
        // FusionMacro -----------------
        run("Decompose ","sigmas=1 scale=0.5 iterations="+decomp_iter+" gamma="+ gamma+"");
        progress = progress + 0.2;
        showProgress(progress);
        showStatus("Fusion in progress...");
        selectWindow(title);
        run("Saliency ");
        progress = progress + 0.2;
        showProgress(progress);
        showStatus("Fusion in progress...");
        selectWindow(title);
        run("Weight ");
        progress = progress + 0.2;
        showProgress(progress);
        showStatus("Fusion in progress...");
        selectWindow(title);
        run("WMAP ", "sigma="+sigma1+" scale="+scale1+" sigma_0="+sigma2+" scale_0="+scale2+" iterations="+wmap_iter);
        progress = progress + 0.2;
        showProgress(progress);
        showStatus("Fusion in progress...");
        selectWindow(title);
        close();
        run("Fusion ", "base=[Base Layer] detail=[Detail Layer] wb=WB wd=WD");
        progress = progress + 0.2;
        showProgress(progress);
        showStatus("Fusion in progress...");
        selectWindow("Base Layer");
        close();
        selectWindow("Detail Layer");
        close();
        selectWindow("WB");
        close();
        selectWindow("WD");
        close();
        selectWindow("Fused Image");
        rename("Fused Image Result of " + title);
        // Convert to 8-bit
        run("8-bit");
        if (input_format=="Stack file")
        {
            fused_image_name = File.getName(input_path);
            fused_image_name = substring(fused_image_name, 0, fused_image_name.length - 4) + "_Fused.tif";
            saveAs("Tiff", File.getParent(input_path) + File.separator + fused_image_name);
    
        }
        else
        {
            // Extract folder name from input_path
            folder_path = substring(input_path, 0, input_path.length - 1); // Remove trailing separator
            folder_name = substring(folder_path, lastIndexOf(folder_path, File.separator)+1, folder_path.length);
            fused_image_name = folder_name + "_Fused.tif";
            saveAs("Tiff", input_path + fused_image_name);
        }
        print("Fusion completed");
        showProgress(1);
   }
        
