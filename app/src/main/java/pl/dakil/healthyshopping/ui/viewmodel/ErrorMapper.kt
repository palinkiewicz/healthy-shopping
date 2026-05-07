package pl.dakil.healthyshopping.ui.viewmodel

import pl.dakil.healthyshopping.data.model.ErrorType
import retrofit2.HttpException
import java.io.IOException

fun Throwable.toErrorType(): ErrorType {
    return when (this) {
        is IOException -> ErrorType.CONNECTION
        is HttpException -> {
            if (this.code() == 404) ErrorType.NOT_FOUND
            else ErrorType.UNKNOWN
        }
        else -> ErrorType.UNKNOWN
    }
}
