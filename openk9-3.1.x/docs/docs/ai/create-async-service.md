---
id: create-async--ml-service
title: Create Async ML service
---

Once connected your data source in Openk9, you can configure an enrich pipeline for it. See more on specific configuration section how to configure an enrich pipeline.

An enrich pipeline is composed by enrich items.

An enrich item can be of different types:

- Groovy Script:
- Http Sync:
- Http Async: 

In case of Http Sync/Async types you can attach Machine Learning models to your pipeline to perform tasks like:

- Summarizzation:
- Entity recognition:
- Translation:
- 

Openk9 offers a powerful integration with Hugging Face to deploy and attach Machine Learning models to your enrich pipeline in a fast and efficient way.

If you want to connect your custom model you need to configure an Http Sync enrich item. In this way you can specify url to reach your model and use it to enrich your data. Read more on specific section about configure Http Sync enrich items.

Otherwise you can wrap your model in a Async service and connect it to Enrich Pipeline as Http Async enrich item. Read more on specific section about wrap your model in a Async service and configure Http Async enrich items.



You can use or apply Machine Learning models and AI techiniques in:

- **data enrichment**: when data is ingested can go through an enrich pipeline to extract new information and enrich original data. You can configure your pipeline to use ML models and enrich your data. See more on specific [section](https://it.lipsum.com/)
- **semantic search**: 
- **chat with your data**: 