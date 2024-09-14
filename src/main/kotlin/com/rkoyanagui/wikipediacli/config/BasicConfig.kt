package com.rkoyanagui.wikipediacli.config

import io.micronaut.context.annotation.Factory
import io.micronaut.inject.InjectionPoint
import jakarta.inject.Singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import reactor.core.scheduler.Scheduler
import reactor.core.scheduler.Schedulers
import java.time.Clock

@Factory
class BasicConfig {

    @Singleton
    fun clock(): Clock {
        return Clock.systemDefaultZone()
    }

    @Singleton
    fun scheduler(): Scheduler {
        return Schedulers.boundedElastic()
    }

    @Singleton
    fun logger(injectionPoint: InjectionPoint<*>): Logger {
        val clazz = injectionPoint.declaringBean.beanType
        return LoggerFactory.getLogger(clazz)
    }
}
