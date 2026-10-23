# GaussianATModelWithCredit
Gaussian AT Model with Authorship Credit Allocation Schemes

## 1. Introduction
The author interest discovery can help personalized academic recommendation systems (i.e., Microsoft Academic and Google Scholar) to answer a range of important questions, such as which themes each researcher prefers to, which scientific publications each scholar is more likely to read, and which researchers are similar to each other in terms of their expertise. In view of the growing volume of online documents available through the Internet, this task is of great practical significance.

Several variants of the LDA model specific for author interest discovery have been put forward in the literature, such as Author-Topic (AT) Model and AT over Time (AToT) Model. On closer examination, it readily makes clear that there is usually a uniform distribution assumption for the author list of a document in these models. That is, each coauthor is implicitly assumed to equally contribute to a target document. Obviously, it is not in line with our intuition.

More effort has been devoted towards the fair assessment of each coauthor's contribution by assigning proper credit awards. Several authorship credit allocation schemes have been proposed in the literature, such as *indiscriminate counting scheme*, *arithmetric counting scheme*, *geometric counting scheme*, *harmonic counting scheme*, *network-based counting scheme*, *axiomatic counting scheme*, and *golden number counting scheme*. 
