// ReviewItem.kt (можно вынести в отдельный файл)
package com.example.movieapplication

import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.movieapplication.ui.theme.Pink40
import com.example.movieapplication.Model.Review
import com.example.movieapplication.Model.UserData
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.foundation.shape.CircleShape

@Composable
fun ReviewItem(
    review: Review,
    userData: UserData?,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (userData?.photoUrl?.isNotEmpty() == true) {
                AsyncImage(
                    model = userData.photoUrl,
                    contentDescription = "https://ibb.co/q3b835dC",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "https://ibb.co/q3b835dC",
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    tint = Color.Gray
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = userData?.name ?: "User", fontSize = 14.sp, color = Pink40)
                Text(text = review.text, fontSize = 14.sp)
            }
        }
    }
}

