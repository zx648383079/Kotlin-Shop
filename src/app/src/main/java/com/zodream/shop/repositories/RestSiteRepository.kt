package com.zodream.shop.repositories

import com.zodream.shop.models.Site

class RestSiteRepository(private val rest: RestRepository) {

    fun getSite(failure: FailureFunc?): Site? {
        return rest.request().fetch(Site::class.java, "shop/home/index", failure)
    }
}