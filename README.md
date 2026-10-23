# GaussianATModelWithCredit
Gaussian AT Model with Authorship Credit Allocation Schemes

## 1. Introduction
### 1.1 Description
The author interest discovery can help personalized academic recommendation systems (i.e., Microsoft Academic and Google Scholar) to answer a range of important questions, such as which themes each researcher prefers to, which scientific publications each scholar is more likely to read, and which researchers are similar to each other in terms of their expertise. In view of the growing volume of online documents available through the Internet, this task is of great practical significance.

Several variants of the Latent Dirichlet Allocation (LDA) model (Blei et al., 2003) specific for author interest discovery have been put forward in the literature, such as Author-Topic (AT) Model (Rosen-Zvi et al., 2010) and AT over Time (AToT) Model (Xu et al., 2014). On closer examination, it readily makes clear that there is usually a uniform distribution assumption for the author list of a document in these models. That is, each coauthor is implicitly assumed to equally contribute to a target document. Obviously, it is not in line with our intuition.

More effort has been devoted towards the fair assessment of each coauthor's contribution by assigning proper credit awards. Thereupon several authorship credit allocation schemes have been proposed in the literature, such as *indiscriminate counting scheme*, *arithmetric counting scheme*, *geometric counting scheme*, *harmonic counting scheme*, *network-based counting scheme*, *axiomatic counting scheme*, and *golden number counting scheme*. Though, until now, no consensus on which one is the bast has been attained.

The AT<sup>crecit</sup> model was proposed in Xu et al. (2021, 2025) to take into consideration authorship credit allocation scheme. However, the AT<sup>credit</sup> model retains some limitations inherited from LDA. For example, words are represented as discrete symbols, and topics are represented as multinomial distributions over a fixed vocabulary. Such representations do not directly capture semantic similarities between words, which may reduce semantic coherence of the inferred topics. They also limit the ability of the model to represent words that are not included in the predefined vocabulary (Das et al., 2015). Hence, a new topic model, Gaussian AT<sup>credit</sup> model, is developed by combining AT<sup>credit</sup> model (Xu et al., 2021; Xu et al., 2025) and Gaussian LDA model (Das et al., 2015) via leveraging the semantic proximity encoded in word embeddings.

### 1.2 News, Comments, and Bug Reports.
We highly appreciate any suggestion, comment, and bug report.

### 1.3 License
Code (c) 2011 Jacob Eisenstein
[Licensed under the Apache License, version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html)
## 2. How to Use Gaussian AT<sup>credit</sup> Model
### 2.1 Data Format
Please refer to the sample in [`data/math_software`](data/math_software).

### 2.2 Command Line & Input Parameters
```
usage: GaussianATModelWithCredit v1.0: to run Gaussian AT<sup>credit</sup> Model.
Author: XU, Shuo (pzczxs@gmail.com)
 -a,--alpha <arg>               alpha (default: 0.5)
 -af,--adjustFlag               to adjust the credits according to
                                hyper-authorship strategy
 -b,--beta <arg>                beta (default: 0.1)
 -D,--numDimensions <arg>       #of dimensions for word embeddings
 -e,--embeddingFileName <arg>   embedding file name
 -fb,--filebase <arg>           file base
 -h,--help                      Print this usage information
 -i,--numInitIterations <arg>   #of initialization iterations by
                                AT<sup>credit</sup> model (default: 0)
 -k,--kappa <arg>               kappa (default: 0.1)
 -K,--numTopics <arg>           #of topics (default: 100)
 -l,--lambda <arg>              lambda (default: 0)
 -n,--numIterations <arg>       #of estimation iterations (default: 50)
 -sc,--scheme <arg>             0: arithmetic counting scheme
                                1: axiomatic counting scheme
                                2: geometric counting scheme
                                3: golden number counting scheme
                                4: harmonic counting scheme
                                5: network-based counting scheme
                                6: single author scheme
                                7: indiscriminate counting scheme
                                (default)
 -se,--seed <arg>               seed for rand (default: 56567651)
 -sf,--stopEarlyFlag            to stop early
 -ta,--numTopAuthors <arg>      #of top authors (default: 20)
 -tw,--numTopWords <arg>        #of top words (default: 20)
 -tw4,--numTopWords4PMI <arg>   #of top words for PMI (default: 15)
```

### 2.3 Resources
Word Embeddings: pre-trained 50-demensional embedding vectors on 2024 Wikipedia + Gigaword 5 with GloVe approach

To select an appropriate number of topics K for the Gaussian AT<sup>credit</sup> model, model performance is evaluated with normalized pointwise mutual information (NPMI) (Lau et al., 2014) and cosine distance. A higher NPMI value indicates semantically more topic coherence, whereas a larger cosine distance signals greater separation among topics. Given a topic k represented by its top-$N$ topic words w_(k,1,) w_(k,2,)⋯w_(k,N), the NPMI score and cosine distance for k are defined formally as follows:

### 2.4 Examples
```
> java -Xmx20g -jar GaussianATCreditModel.jar -sf -D 50 -e resources/glove.2024.wikigiga.50d.zip -n 50 -sc 7 -fb data/math_software/math_software -K 50
```

### 2.5 Additional Information
This tool is written by [XU, Shuo](http://54xushuo.net/wiki/) from [Beijing University of Technology](http://www.bjut.edu.cn). If you find this tool useful, please cite Gaussian AT<sup>credit</sup> model as follows: 

Shuo Xu, Lanzhi Li, Jiahao Li, Xin An, Jianhua Liu, and Xuegang Deng, 2026. Evaluation of Mathematical Software Impact based on Diversity Measures. (Under Review)

For any question, please contact XU, Shuo xushuo@bjut.edu.cn OR pzczxs@gmail.com.
## 3. References
[1] David M. Blei, Andrew Y. Ng, and Michael I. Jordan, 2003. [Latent Dirichlet Allocation](http://jmlr.csail.mit.edu/papers/v3/blei03a.html). *Journal of Machine Learning Research*, Vol. 3, No. Jan, pp. 993-1022.

[2] Michal Rosen-Zvi, Chaitanya Chemudugunta, Thomas Griffiths, and Padhraic Smyth, and Mark Steyvers, 2010. [Learning Author-Topic Models from Text Corpora](https://cocosci.berkeley.edu/tom/papers/AT_tois.pdf). *ACM Transactions on Information Systems*, Vol. 28, No. 1, pp. 1-38. 

[3] Shuo Xu, Qingwei Shi, Xiaodong Qiao, Lijun Zhu, Han Zhang, Hanmin Jung, Seungwoo Lee, and Sung-Pil Choi, 2014. [A Dynamic Users' Interest Discovery Model with Distributed Inference Algorithm](https://doi.org/10.1155/2014/280892). *International Journal of Distributed Sensor Networks*, Vol. 2014, pp. 1-11.

[4] Shuo Xu, Ling Li, Congcong Wang, Xin An, and Guancan Yang, 2025. [An Improved Author-Topic (AT) Model with Authorship Credit Allocation Schemes](https://doi.org/10.1177/01655515221133530). *Journal of Information Science*, Vol. 50, No. 1, pp. 184-204.

[5] Shuo Xu, Ling Li, Liyuan Hao, Xin An, and Guancan Yang, 2021. [An Author Interest Discovery Model armed with Authorship Credit Allocation Scheme](https://doi.org/10.1007/978-3-030-71292-1_18). *iConference: Diversity, Divergence, Dialogue*, pp. 199-207. 

[6] Rajarshi Das, Manzil Zaheer, and Chris Dyer, 2015. [Gaussian LDA for Topic Models with Word Embeddings](https://doi.org/10.3115/v1/P15-1077). *Proceedings of the 53rd Annual Meeting of the Association for Computational Linguistics and the 7th International Joint Conference on Natural Language Processing, Association for Computational Linguistics*, pp. 795-804. 

[7] Jey Han Lau, David Newman, and Timothy Baldwin, 2014. [Machine Learning Tea Leaves: Automatically Evaluating Topic Coherence and Topic Model Quality](https://doi.org/10.3115/v1/E14-1056). *Proceedings of the 14th Conference of the European Chapter of the Association for Computational Linguistics, Association for Computational Linguistics*, pp. 530-539. 

[8] Shuo Xu, Lanzhi Li, Jiahao Li, Xin An, Jianhua Liu, and Xuegang Deng, 2026. Evaluation of Mathematical Software Impact based on Diversity Measures. (Under Review)
