package org.smarthata.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler

@Configuration
@EnableScheduling
@EnableAsync
class AppConfig {
    @Bean
    fun taskScheduler(): TaskScheduler = ThreadPoolTaskScheduler().apply { poolSize = 2 }
}
