# GaussianATModelWithCredit
Gaussian AT Model with Authorship Credit Allocation Schemes

## 1. Introduction
### 1.1 Description
The author interest discovery can help personalized academic recommendation systems (i.e., Microsoft Academic and Google Scholar) to answer a range of important questions, such as which themes each researcher prefers to, which scientific publications each scholar is more likely to read, and which researchers are similar to each other in terms of their expertise. In view of the growing volume of online documents available through the Internet, this task is of great practical significance.

Several variants of the Latent Dirichlet Allocation (LDA) model (Blei et al., 2003) specific for author interest discovery have been put forward in the literature, such as Author-Topic (AT) Model () and AT over Time (AToT) Model (Xu et al., 2014). On closer examination, it readily makes clear that there is usually a uniform distribution assumption for the author list of a document in these models. That is, each coauthor is implicitly assumed to equally contribute to a target document. Obviously, it is not in line with our intuition.

More effort has been devoted towards the fair assessment of each coauthor's contribution by assigning proper credit awards. Thereupon several authorship credit allocation schemes have been proposed in the literature, such as *indiscriminate counting scheme*, *arithmetric counting scheme*, *geometric counting scheme*, *harmonic counting scheme*, *network-based counting scheme*, *axiomatic counting scheme*, and *golden number counting scheme*. Though, until now, no consensus on which one is the bast has been attained.

The AT<sup>crecit</sup> model was proposed in Xu et al. (2021, 2025) to take into consideration authorship credit allocation scheme. However, the AT<sup>credit</sup> model retains some limitations inherited from LDA. For example, words are represented as discrete symbols, and topics are represented as multinomial distributions over a fixed vocabulary. Such representations do not directly capture semantic similarities between words, which may reduce semantic coherence of the inferred topics. They also limit the ability of the model to represent words that are not included in the predefined vocabulary (Das et al., 2015). Hence, a new topic model, Gaussian AT<sup>credit</sup> model, is developed by combining AT<sup>credit</sup> model (Xu et al., 2021; Xu et al., 2025) and Gaussian LDA model (Das et al., 2015) via leveraging the semantic proximity encoded in word embeddings.

### 1.2 News, Comments, and Bug Reports.
We highly appreciate any suggestion, comment, and bug report.

### 1.3 License
Code (c) 2011 Jacob Eisenstein
[Licensed under the Apache License, version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)
## 2. How to Use Gaussian AT<sup>credit</sup> Model

## 3. References
[1] David M. Blei, Andrew Y. Ng, and Michael I. Jordan, 2003. [Latent Dirichlet Allocation](http://jmlr.csail.mit.edu/papers/v3/blei03a.html). *Journal of Machine Learning Research*, Vol. 3, No. Jan, pp. 993-1022.
[2] Michal Rosen-Zvi, Chaitanya Chemudugunta, Thomas Griffiths, and Padhraic Smyth, and Mark Steyvers, 2010. [Learning Author-Topic Models from Text Corpora](https://cocosci.berkeley.edu/tom/papers/AT_tois.pdf). *ACM Transactions on Information Systems*, Vol. 28, No. 1, pp. 1-38. 
[3] Shuo Xu, Qingwei Shi, Xiaodong Qiao, Lijun Zhu, Han Zhang, Hanmin Jung, Seungwoo Lee, and Sung-Pil Choi, 2014. [A Dynamic Users' Interest Discovery Model with Distributed Inference Algorithm](https://doi.org/10.1155/2014/280892). *International Journal of Distributed Sensor Networks*, Vol. 2014, pp. 1-11.
[4] Shuo Xu, Ling Li, Congcong Wang, Xin An, and Guancan Yang, 2025. [An Improved Author-Topic (AT) Model with Authorship Credit Allocation Schemes](https://doi.org/10.1177/01655515221133530). *Journal of Information Science*, Vol. 50, No. 1, pp. 184-204.
[5] Shuo Xu, Ling Li, Liyuan Hao, Xin An, and Guancan Yang, 2021. [An Author Interest Discovery Model armed with Authorship Credit Allocation Scheme](https://doi.org/10.1007/978-3-030-71292-1_18). *iConference: Diversity, Divergence, Dialogue*, pp. 199-207. 
[6] Rajarshi Das, Manzil Zaheer, and Chris Dyer, 2015. [Gaussian LDA for Topic Models with Word Embeddings](https://doi.org/10.3115/v1/P15-1077). *Proceedings of the 53rd Annual Meeting of the Association for Computational Linguistics and the 7th International Joint Conference on Natural Language Processing, Association for Computational Linguistics*, pp. 795-804. 
[7] Shuo Xu, Lanzhi Li, Jiahao Li, Xin An, Jianhua Liu, and Xuegang Deng, 2026. Evaluation of Mathematical Software Impact based on Diversity Measures. (Under Review)
