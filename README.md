# 🏠 HomeServ — Home Services Marketplace

An Android application that connects homeowners with professional home service providers.

---

## 📱 Features

### Customer
- Browse service providers by category
- Search & filter by name, price, and rating
- View provider location on Google Maps
- Book services with date/time scheduling
- Track order status
- Rate completed services

### Admin
- Dashboard with statistics
- Manage service categories (Add / Edit / Delete)
- Manage providers with map location picker (Add / Edit / Delete)
- Manage and update service requests status

---

## 🛠️ Tech Stack

| Technology | Usage |
|-----------|-------|
| Kotlin | Primary language |
| MVVM Architecture | Design pattern |
| Firebase Authentication | User login/register |
| Firebase Firestore | Cloud database |
| Google Maps SDK | Maps & location |
| Navigation Component | Screen navigation |
| Coroutines + LiveData | Async & reactive UI |
| Material Design 3 | UI components |
| Glide | Image loading |
| ViewBinding | View access |
| Shimmer | Loading skeleton |

---

## 🏗️ Architecture

```
UI Layer (Fragments/Activities)
        ↕ LiveData
ViewModel Layer
        ↕ suspend functions
Repository Layer
        ↕ await()
Firebase Firestore / Local Storage
```

---

## 🗄️ Database Structure (Firestore)

```
├── users/          → User profiles & roles
├── providers/      → Service provider listings
├── categories/     → Service categories
└── requests/       → Service booking requests
```

---

## 🚀 Setup Instructions

### Prerequisites
- Android Studio Meerkat or later
- Android SDK 36
- Google Maps API Key
- Firebase project

### Steps

1. **Clone the repository**
   ```bash
   git clone https://github.com/YOUR_USERNAME/HomeServ.git
   ```

2. **Open in Android Studio**

3. **Add `google-services.json`**
    - Go to [Firebase Console](https://console.firebase.google.com)
    - Download `google-services.json`
    - Place it in `app/` folder

4. **Add Google Maps API Key**
    - Open `app/src/main/res/values/strings.xml`
    - Replace `YOUR_MAPS_API_KEY_HERE` with your key

5. **Create Admin Account**
    - Go to Firebase Console → Authentication → Add user
    - Add user document in Firestore `users` collection with `role: "admin"`

6. **Sync & Run**
    - Click **Sync Now** in Android Studio
    - Run on emulator or physical device (API 26+)

---

## 🔐 Firebase Security Rules

The app uses Firestore security rules to ensure:
- Customers can only read/write their own data
- Only admin can manage providers and categories
- Customers can only update the `rating` field on their own completed requests

---

## 📸 Screenshots

> Add screenshots here after running the app

---

## 👨‍💻 Developer

Built with ❤️ using Kotlin + Firebase