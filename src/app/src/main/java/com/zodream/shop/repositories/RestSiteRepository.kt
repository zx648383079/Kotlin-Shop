package com.zodream.shop.repositories

import com.zodream.shop.models.Site

class RestSiteRepository(private val rest: RestRepository) {

    fun getSite(success: (Site) -> Unit, failure: FailureFunc?) {
        return rest.request().fetch(Site::class.java, "shop/home/index", success, failure)
    }

    fun getSite(success: (Site) -> Unit) {
        return getSite(success, null)
    }
}