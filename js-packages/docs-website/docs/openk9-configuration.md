---
id: openk9-configuration
title: Openk9 configuration
---

## Openk9 tenant and datasource configuration

After having prepared the * core * component of OpenK9, I can proceed with the creation of an example Tenant,
connected to the Realm that I previously registered in Keycloak, where to test the product.

Access to [administration console](http://demo.openk9.local/admin) e goes to "Tenants" section.

![image-20220303222133620](../static/img/installation/image-20220303222133620.png)

Add new tenant with name "demo.openk9.local" (use both as name and virtualHost)

![image-20220303222526604](../static/img/installation/image-20220303222526604.png)

Modify tenant Json configuration adding following and save:

```json
{
    "querySourceBarShortcuts": [
        {
            "id": "web",
            "text": "web"
        },
        {
            "id": "document",
            "text": "document"
        }
    ]
}
```

![image-20220303222734091](../static/img/installation/image-20220303222734091.png)

Now create new "Datasource".

![image-20220303223519664](../static/img/installation/image-20220303223519664.png)

The information present in the "datasource" is acquired through a plugin.
Basically, only a few generic plugins are available; we will see you can how to add new plugins. +
As "Driver Service Name" I choose `SitemapWebPluginDriver` to acquire the contents of a website using a crawler that relies on its sitemap.

![image-20220303223805366](../static/img/installation/image-20220303223805366.png)

The basic configuration proposes the scan of the SMC official website with a schedule of 30
minutes from the beginning of the hour. I can accept the defaults or change them. Then "Save".

![image-20220303225013186](../static/img/installation/image-20220303225013186.png)

Go inside datasource detail and click on "Enrich" card.

![image-20220304125618859](../static/img/installation/image-20220304125618859.png)

Now create a new Enrich Pipeline

![image-20220304125654526](../static/img/installation/image-20220304125654526.png)

Use "ADD" button to create new Enirch Item

![image-20220304125721960](../static/img/installation/image-20220304125721960.png)

che sfrutterà la "NER", o meglio, che sfrutterà il componente "AsyncWebNerEnrichProcessor" che contiene le logiche per elaborare in modo asincrono informazioni provenienti da un sito web. Quindi "Save" per accettare i valori di default.

![image-20220304125804897](../static/img/installation/image-20220304125804897.png)

Mi viene mostrato il dettaglio del task appena creato.

![image-20220304125823164](../static/img/installation/image-20220304125823164.png)

Mentre se ritorno nella scheda Enrich vedo il task posizionato graficamente nel flusso.

![image-20220304125842958](../static/img/installation/image-20220304125842958.png)
