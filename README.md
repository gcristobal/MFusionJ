# MFusionJ
This novel ImageJ plugin is designed to significantly enhance the depth-of-field (DoF) by seamlessly merging a series of multi-focus images, allowing for in-depth analysis. The plugin has also been rigorously tested on multi-exposure image stacks, demonstrating its adeptness in preserving intricate details within both poorly and brightly illuminated regions of 3-D transparent diatom shells.

The significance of this capability becomes particularly apparent when dealing with images that exhibit a limited DoF and varying exposure settings under low signal-to-noise ratio conditions. The plugin's effectiveness has been thoroughly validated through the processing and analysis of numerous image stacks featuring diverse diatom and cyanobacteria species.

The proposed methodology incorporates a sophisticated two-scale decomposition (TSD) scheme, complemented by the refinement of weight maps using edge-preserving filtering (EPF). This dual approach ensures the preservation of fine details in the fused image while simultaneously minimizing noise. Such innovations make this plugin a valuable tool for researchers and analysts working with complex image datasets. 

# Multifocus fusion example

![A401 5-10](https://github.com/gcristobal/MFusionJ/assets/1918777/f3871f37-6f67-4514-9c40-e2423ccff806) ![A401 6](https://github.com/gcristobal/MFusionJ/assets/1918777/0dfe79b2-9f7f-4e48-96ff-7b31f2bdd7ae) ![A401 7](https://github.com/gcristobal/MFusionJ/assets/1918777/60c01081-3063-4264-83f5-15741b3cf107) ![A401 8](https://github.com/gcristobal/MFusionJ/assets/1918777/b13f0b4d-d207-405d-9266-44b1b384a170) ![A401 9](https://github.com/gcristobal/MFusionJ/assets/1918777/0ce29cc2-c589-40eb-a6b6-9810975113ef) Original images

![Fused Image](https://github.com/gcristobal/MFusionJ/assets/1918777/69c8f08c-0111-4f44-b411-25de612ac7bb) Final fused image







