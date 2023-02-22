package com.astrum.data.configuration

import com.astrum.data.annotation.ConverterScope
import io.r2dbc.spi.ConnectionFactory
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Configuration
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter
import org.springframework.data.r2dbc.config.AbstractR2dbcConfiguration

@Configuration
class R2dbcConfiguration(
    private val connectionFactory: ConnectionFactory,
    private val applicationContext: ApplicationContext
) : AbstractR2dbcConfiguration() {
    override fun connectionFactory(): ConnectionFactory {
        return connectionFactory
    }

    override fun getCustomConverters(): MutableList<Converter<*, *>> {
        val converterList = mutableListOf<Converter<*, *>>()
        val converters = applicationContext.getBeansOfType(Converter::class.java)
            .values
            .filter {
                it.javaClass.annotations.any { annotation -> annotation is WritingConverter || annotation is ReadingConverter }
            }
            .filter {
                val scope = it.javaClass.annotations.filterIsInstance<ConverterScope>()
                scope.isEmpty() || scope.any { converterScope -> converterScope.type == ConverterScope.Type.R2DBC }
            }
        converterList.addAll(converters)

        return converterList
    }
}
