package pl.dakil.healthyshopping.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import pl.dakil.healthyshopping.data.model.ErrorType

@Composable
fun StandardError(
    errorType: ErrorType,
    modifier: Modifier = Modifier,
    customMessage: String? = null,
    onRetry: (() -> Unit)? = null
) {
    val (icon, title, description) = when (errorType) {
        ErrorType.CONNECTION -> Triple(
            Icons.Default.CloudOff,
            "Brak połączenia",
            "Upewnij się, że masz dostęp do internetu i spróbuj ponownie."
        )
        ErrorType.NOT_FOUND -> Triple(
            Icons.Default.SearchOff,
            "Nie znaleziono",
            "Niestety nie udało się odnaleźć szukanego elementu w naszej bazie."
        )
        ErrorType.UNKNOWN -> Triple(
            Icons.Default.ErrorOutline,
            "Wystąpił błąd",
            "Coś poszło nie tak. Spróbuj ponownie za chwilę."
        )
    }

    val displayMessage = customMessage ?: description

    Column(
        modifier = modifier
            .padding(24.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = displayMessage,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (onRetry != null) {
            Spacer(modifier = Modifier.height(32.dp))
            Button(
                onClick = onRetry,
                shape = CircleShape,
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 12.dp)
            ) {
                Text("Spróbuj ponownie")
            }
        }
    }
}
