# 1.1 Word Embeddings
The pre-trained word embedings are needed for the Gaussian AT<sup>credit</sup> model in advance. One can obtain word embeddings from [Google word2vec](https://code.google.com/archive/p/word2vec/), [GloVe](https://nlp.stanford.edu/projects/glove/), [FastText](https://fasttext.cc/docs/en/pretrained-vectors.html), or a vector LLM. For computational efficiency considerations，50-dimensional word embedding are preferred. Please download the pre-trained embedding vectors from [GloVe](https://nlp.stanford.edu/projects/glove/), and then put them in the directory `resources`. 

# 1.2 Statistics for single terms and term pairs from Wikipedia articles
To select an appropriate number of topics $$K$$ for the Gaussian AT<sup>credit</sup> model, model performance is evaluated with normalized pointwise mutual information (NPMI) (Lau et al., 2014) and cosine distance. A higher NPMI value indicates semantically more topic coherence, whereas a larger cosine distance signals greater separation among topics. Given a topic $$k$$ represented by its top-$`N`$ topic words $w_{k, 1}, w_{k, 2}, \cdots, w_{k, N}$, the NPMI score and cosine distance for $$k$$ are defined formally as follows:

$$\mathrm{NPMI}(k) = \sum_{1\le i\le j\le N} \frac{\log \dfrac{\mathrm{Pr}(w_{k,i},w_{k,j})}{\mathrm{Pr}(w_{k,i})\mathrm{Pr}(w_{k,j})}}{-\log\mathrm{Pr}(w_{k,i},w_{k,j})}$$

$$\mathrm{dist\_cos}(k) = \frac{2}{N(N-1)} \sum_{1\le i\le j\le N} \Big(1-\cos(\vec{e}_{w_{k,i}},\vec{e}_{w_{k,j}})\Big)$$

where $$\mathrm{Pr}(\cdot, \cdot)$$ and $$\mathrm{Pr}(\cdot)$$ are derived from a 10-word sliding window over English Wikipedia. The statistics for single terms and term pairs have been calculated from a 10-word sliding window over the Wikipedia articles dump of March 1, 2026. Please download them from [Figshare](https://doi.org/10.6084/m9.figshare.34711200), and then put them in the directory `resources`.
