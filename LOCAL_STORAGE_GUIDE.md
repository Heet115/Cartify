# Local Storage Implementation Guide for Cartify App

This guide explains how to use the local storage system implemented for your Cartify Android app.

## Overview

The local storage system consists of two main classes:
- `LocalStorageManager`: Low-level SharedPreferences wrapper
- `UserDataHelper`: High-level helper for common operations

## Key Features

### 1. User Authentication Data
- Save/retrieve user login information
- Session management
- Auto-login functionality

### 2. Shopping Cart Data
- Persistent cart across app sessions
- Add/remove/update cart items
- Calculate cart totals

### 3. User Preferences
- App theme (light/dark)
- Notification settings
- Language preferences
- Currency settings

### 4. User Behavior Data
- Favorite products
- Recent search queries
- First-time user tracking
- Last login time

### 5. Generic Data Storage
- Save any custom data types
- JSON serialization for complex objects

## Usage Examples

### Basic Setup

```java
// In your Activity
UserDataHelper userDataHelper = new UserDataHelper(this);
```

### User Authentication

```java
// Save user login data
userDataHelper.saveUserSession("userId123", "user@example.com", "John Doe");

// Check if user is logged in
if (userDataHelper.isUserLoggedIn()) {
    // User is logged in
}

// Logout user
userDataHelper.logoutUser();
```

### Shopping Cart Operations

```java
// Add item to cart
CartItem item = new CartItem();
item.setProductId("product123");
item.setTitle("Sample Product");
item.setPrice(29.99);
item.setQuantity(2);
userDataHelper.addToCart(item);

// Get cart items
List<CartItem> cartItems = userDataHelper.getCartItems();

// Get cart totals
int itemCount = userDataHelper.getCartItemCount();
double total = userDataHelper.getCartTotal();

// Update item quantity
userDataHelper.updateCartItemQuantity("product123", 3);

// Remove item from cart
userDataHelper.removeFromCart("product123");

// Clear entire cart
userDataHelper.clearCart();
```

### Favorites Management

```java
// Toggle favorite status
userDataHelper.toggleFavorite("product123");

// Check if product is favorite
boolean isFavorite = userDataHelper.isFavorite("product123");
```

### Search History

```java
// Add search query
userDataHelper.addSearchQuery("smartphone");

// Get recent searches (returns last 10)
List<String> recentSearches = userDataHelper.getRecentSearches();
```

### App Preferences

```java
// Save app preferences
userDataHelper.saveAppPreferences("dark", true, "en");

// Get individual preferences
String theme = userDataHelper.getAppTheme(); // "dark" or "light"
boolean notificationsEnabled = userDataHelper.areNotificationsEnabled();
String language = userDataHelper.getLanguage();
```

### Custom Data Storage

```java
// Using LocalStorageManager directly for custom data
LocalStorageManager storage = LocalStorageManager.getInstance(this);

// Save custom object
MyCustomObject obj = new MyCustomObject();
storage.saveObject("my_custom_key", obj);

// Retrieve custom object
MyCustomObject retrieved = storage.getObject("my_custom_key", MyCustomObject.class);

// Save primitive types
storage.saveString("custom_string", "value");
storage.saveInt("custom_int", 42);
storage.saveBoolean("custom_bool", true);
```

## Integration with Existing Activities

### LoginActivity Integration

```java
public class LoginActivity extends AppCompatActivity {
    private UserDataHelper userDataHelper;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        userDataHelper = new UserDataHelper(this);
        
        // Check if already logged in
        if (userDataHelper.isUserLoggedIn()) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }
    }
    
    private void onLoginSuccess(FirebaseUser user, String email) {
        // Save user data locally
        userDataHelper.saveUserSession(user.getUid(), email, user.getDisplayName());
        userDataHelper.updateLastLoginTime();
        userDataHelper.setFirstTimeUser(false);
    }
}
```

### MainActivity Integration

```java
public class MainActivity extends AppCompatActivity {
    private UserDataHelper userDataHelper;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        userDataHelper = new UserDataHelper(this);
        
        // Apply saved theme
        applyTheme(userDataHelper.getAppTheme());
    }
    
    private void onLogout() {
        userDataHelper.logoutUser();
        // Optionally clear cart: userDataHelper.clearCart();
    }
}
```

### ProductAdapter Integration

```java
public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {
    private UserDataHelper userDataHelper;
    
    public ProductAdapter(Context context, List<Product> products) {
        this.userDataHelper = new UserDataHelper(context);
        // ... other initialization
    }
    
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Product product = products.get(position);
        
        // Set favorite icon based on local storage
        boolean isFavorite = userDataHelper.isFavorite(product.getId());
        holder.favoriteIcon.setImageResource(isFavorite ? 
            R.drawable.ic_favorite_filled : R.drawable.ic_favorite_outline);
        
        // Handle favorite click
        holder.favoriteIcon.setOnClickListener(v -> {
            userDataHelper.toggleFavorite(product.getId());
            notifyItemChanged(position);
        });
        
        // Handle add to cart
        holder.addToCartButton.setOnClickListener(v -> {
            CartItem cartItem = new CartItem();
            cartItem.setProductId(product.getId());
            cartItem.setTitle(product.getName());
            cartItem.setPrice(product.getPrice());
            cartItem.setQuantity(1);
            
            userDataHelper.addToCart(cartItem);
            Toast.makeText(context, "Added to cart", Toast.LENGTH_SHORT).show();
        });
    }
}
```

## Best Practices

### 1. Initialize Once
Create UserDataHelper instance once per Activity and reuse it.

### 2. Background Operations
For heavy operations, consider using background threads:

```java
// For large data operations
new Thread(() -> {
    // Perform heavy local storage operations
    runOnUiThread(() -> {
        // Update UI
    });
}).start();
```

### 3. Data Validation
Always validate data before saving:

```java
if (productId != null && !productId.trim().isEmpty()) {
    userDataHelper.toggleFavorite(productId);
}
```

### 4. Error Handling
Handle potential errors gracefully:

```java
try {
    List<CartItem> items = userDataHelper.getCartItems();
    // Process items
} catch (Exception e) {
    // Handle error, maybe show default empty cart
    Log.e("CartError", "Failed to load cart items", e);
}
```

### 5. Memory Management
Don't hold references to large objects in memory. Load data when needed.

## Data Persistence

All data is automatically persisted using Android's SharedPreferences, which means:
- Data survives app restarts
- Data survives device reboots
- Data is cleared only when app is uninstalled or user clears app data
- Data is private to your app

## Security Considerations

- Don't store sensitive data like passwords or credit card numbers
- For sensitive data, consider using Android Keystore
- The current implementation is suitable for user preferences, cart data, and non-sensitive user information

## Testing Your Implementation

Use the `LocalStorageExampleActivity` to test various local storage operations:

1. Add the activity to your AndroidManifest.xml
2. Create a simple layout for testing
3. Run the app and test different storage operations

This comprehensive local storage system will help you create a smooth user experience by persisting important user data across app sessions.