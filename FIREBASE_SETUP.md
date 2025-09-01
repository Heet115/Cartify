# Firebase Setup Instructions

## Step 1: Upload Data to Firebase Realtime Database

You have two options to upload your database.json data to Firebase:

### Option A: Manual Upload (Recommended for beginners)

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project
3. Go to **Realtime Database** in the left sidebar
4. Click on **Create Database** if you haven't already
5. Choose **Start in test mode** for now
6. Once the database is created, click on the **Import JSON** button
7. Select your `database/database.json` file
8. Click **Import**

### Option B: Programmatic Upload (Advanced)

1. Install Node.js if you haven't already
2. Run the following commands in your project root:
   ```bash
   npm install firebase-admin
   ```
3. Go to Firebase Console > Project Settings > Service Accounts
4. Click "Generate new private key" and download the JSON file
5. Rename it to `serviceAccountKey.json` and place it in the project root
6. Update the `databaseURL` in `upload-to-firebase.js` with your Firebase project URL
7. Run: `node upload-to-firebase.js`

## Step 2: Update Firebase Database Rules

Go to Firebase Console > Realtime Database > Rules and update them:

```json
{
  "rules": {
    "Category": {
      ".read": true,
      ".write": "auth != null"
    },
    "Banner": {
      ".read": true,
      ".write": "auth != null"
    },
    "Items": {
      ".read": true,
      ".write": "auth != null"
    },
    "users": {
      "$uid": {
        ".read": "$uid === auth.uid",
        ".write": "$uid === auth.uid"
      }
    }
  }
}
```

## Step 3: Verify Data Structure

After uploading, your Firebase Realtime Database should have this structure:

```
your-project-id-default-rtdb
├── Banner/
│   ├── 0/
│   │   └── url: "https://res.cloudinary.com/..."
│   └── 1/
│       └── url: "https://res.cloudinary.com/..."
├── Category/
│   ├── 0/
│   │   ├── id: 0
│   │   └── title: "All"
│   ├── 1/
│   │   ├── id: 1
│   │   └── title: "Women"
│   └── ...
└── Items/
    ├── 0/
    │   ├── description: "Lorem ipsum..."
    │   ├── oldPrice: 45
    │   ├── picUrl: ["https://res.cloudinary.com/..."]
    │   ├── price: 35
    │   ├── rating: 4.6
    │   └── ...
    └── ...
```

## Step 4: Test the App

1. Build and run your Android app
2. You should see:
   - Categories at the top (horizontal scrollable)
   - Banner images in the middle (swipeable)
   - Products at the bottom (grid layout)

## Troubleshooting

- **No data showing**: Check your internet connection and Firebase rules
- **Images not loading**: Verify the Cloudinary URLs are accessible
- **Build errors**: Make sure all dependencies are properly added to your `build.gradle` files

## Next Steps

- Implement category filtering functionality
- Add search functionality
- Implement user authentication
- Add cart and order management features