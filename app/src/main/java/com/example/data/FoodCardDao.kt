package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodCardDao {
    @Query("SELECT * FROM restaurant_cards ORDER BY createdAt DESC")
    fun getAllCards(): Flow<List<RestaurantCardEntity>>

    @Query("SELECT * FROM restaurant_cards WHERE id = :id LIMIT 1")
    suspend fun getCardById(id: Long): RestaurantCardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: RestaurantCardEntity): Long

    @Update
    suspend fun updateCard(card: RestaurantCardEntity)

    @Delete
    suspend fun deleteCard(card: RestaurantCardEntity)

    @Query("DELETE FROM restaurant_cards WHERE id = :id")
    suspend fun deleteCardById(id: Long)

    @Query("SELECT * FROM food_master_profile WHERE id = 1 LIMIT 1")
    fun getProfile(): Flow<FoodMasterProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProfile(profile: FoodMasterProfileEntity)
}
