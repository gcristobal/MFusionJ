## MFusionJ
This novel ImageJ plugin is designed to significantly enhance the depth-of-field (DoF) by seamlessly merging a series of multi-focus images, allowing for in-depth analysis. The plugin has also been rigorously tested on multi-exposure image stacks, demonstrating its adeptness in preserving intricate details within both poorly and brightly illuminated regions of 3-D transparent diatom shells.

The significance of this capability becomes particularly apparent when dealing with images that exhibit a limited DoF and varying exposure settings under low signal-to-noise ratio conditions. The plugin's effectiveness has been thoroughly validated through the processing and analysis of numerous image stacks featuring diverse diatom and cyanobacteria species.

The proposed methodology incorporates a sophisticated two-scale decomposition (TSD) scheme, complemented by the refinement of weight maps using edge-preserving filtering (EPF). This dual approach ensures the preservation of fine details in the fused image while simultaneously minimizing noise. Such innovations make this plugin a valuable tool for researchers and analysts working with complex image datasets. 

### Install
MFusionJ is an ImageJ/Fiji (64-bit) plugin and requires ImageJ/FIji to be installed on the computer.
Unzip the downloaded file, and place the resultant MFusionJ folder in the plugins folder of your local ImageJ/Fiji directory. Open ImageJ/Fiji (restart ImageJ/Fiji if it is already open) and MFusionJ should be available to use from the Plugins dropdown menu.

### Usage
First convert the individual images to stacks. From the Image menu follow: Image->Stacks->Images to Stack 
* In the case of multifocus images, apply the macro: Fusion_multifocus.ijm
* In the case of multiexposure images, apply the macro: Fusion_multiexposure.ijm
  
The macros can accept both 8-bit and RGB stacks.

### Multifocus fusion example

Focus#1 | Focus#2  | Focus#3 |Fused result
:------:|:------:|:------:|:------:
[<img src="./images/1.jpg" height="150" hspace="20">](./TheBOX)|[<img src="./images/2.jpg" height="150">](./CAD)|[<img src="./images/3.jpg" height="150">](./APPLICATIONS)|[<img src="./images/fus.jpg" height="150">](./WORKSHOP)

### 3D plot using the fused image as a texture on the surface
For a better visualization of the surface of the object the [Interactive 3D Surface Plot](https://imagej.net/ij/plugins/surface-plot-3d.html) plugin can be used.
3D plot |
:------: |
[<img src="./images/plot.jpg" height="200">](./CAD)

### Multiexposure fusion example

Exp.time: 1/60 | Exp.time: 1/100  | Exp.time: 1/160 |Fused result
:------:|:------:|:------:|:------:
[<img src="./images/60.png" height="150" hspace="20">](./TheBOX)|[<img src="./images/100.png" height="150">](./CAD)|[<img src="./images/160.png" height="150">](./APPLICATIONS)|[<img src="./images/fused_exp.png" height="150">](./WORKSHOP)


**If you find this work useful, please cite**:

	@inproceedings{singh24,
	  author={Singh, H. and Forero, M. and Agaoglu, N. and Bueno, G. and Deniz, O. and Cristobal, G.},
	  booktitle={Proc. SPIE Optics, Photonics, and Digital Technologies for Imaging Applications (April 2024), Strasbourg, France},
	  title={An ImageJ plugin for image fusion based on edge-preserving filtering},
	  year={2024},
	  volume = {},
	  pages = {},
	  doi={}
	}









