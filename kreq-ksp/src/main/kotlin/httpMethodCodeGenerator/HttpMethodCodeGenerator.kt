package httpMethodCodeGenerator

import com.squareup.kotlinpoet.FunSpec
import context.ApiBuildContext
import context.SymbolProcessorContext

/**
 * 该接口定义了如何建立对象方法实现的接口
 * [resolve] 负责具体实现对应的接口
 * [supportMethod] 返回支持的实现的HTTP方法
 */
interface HttpMethodCodeGenerator {
    context (SymbolProcessorContext, ApiBuildContext, FunSpec.Builder)
    fun resolve()

    fun supportMethod(): HttpMethods
}