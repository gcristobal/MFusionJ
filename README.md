## MFusionJ
This novel ImageJ plugin is designed to significantly enhance the depth-of-field (DoF) by seamlessly merging a series of multi-focus images, allowing for in-depth analysis. The plugin has also been rigorously tested on multi-exposure image stacks, demonstrating its adeptness in preserving intricate details within both poorly and brightly illuminated regions of 3-D transparent diatom shells.

The significance of this capability becomes particularly apparent when dealing with images that exhibit a limited DoF and varying exposure settings under low signal-to-noise ratio conditions. The plugin's effectiveness has been thoroughly validated through the processing and analysis of numerous image stacks featuring diverse diatom and cyanobacteria species.

The proposed methodology incorporates a sophisticated two-scale decomposition (TSD) scheme, complemented by the refinement of weight maps using edge-preserving filtering (EPF). This dual approach ensures the preservation of fine details in the fused image while simultaneously minimizing noise. Such innovations make this plugin a valuable tool for researchers and analysts working with complex image datasets. 

## Multifocus fusion example
![1](https://github.com/gcristobal/MFusionJ/assets/1918777/7e31162f-3365-4726-9f1d-9267b51264da) ![2](https://github.com/gcristobal/MFusionJ/assets/1918777/62cce3fe-1866-48bb-9e60-9becba693321) ![3](https://github.com/gcristobal/MFusionJ/assets/1918777/09f42468-4b07-4477-809c-3dbe8022d150) 
![f](https://github.com/gcristobal/MFusionJ/assets/1918777/9ed91dc1-dbb1-4acb-9e5d-8d0218db50f8) ![plot](https://github.com/gcristobal/MFusionJ/assets/1918777/74e8583e-3de4-4a1b-9075-1259641bc7ad) 


`$ npm install marked`







