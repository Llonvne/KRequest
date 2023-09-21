package utils

import com.google.devtools.ksp.getClassDeclarationByName
import com.google.devtools.ksp.processing.Resolver

fun Resolver.getClassDeclarationByNameOrException(name: String) =
    getClassDeclarationByName(name) ?: throw ClassNotFoundException(name)