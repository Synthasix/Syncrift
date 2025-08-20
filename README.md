# Syncrift

**Syncrift** is a dynamic online platform for real-time 1v1 battles in coding, typing, and creative design. Whether you’re a developer, speed typist, or pixel-perfectionist, Syncrift turns your skills into intense matchups against friends and rivals.

---

## Features

- **Real-Time Battles:** Engage in head-to-head matches in coding, typing, and design.  
- **Friend-Based Matchmaking:** Challenge friends directly for bragging rights.  
- **Synchronization:** Enjoy seamless real-time interactions and score updates.  
- **Skill-Based Matchmaking (Under Development):** Compete with rivals of similar skill levels.  
- **Gamified Competition (Under Development):** Earn ranks, badges, and rewards as you climb the leaderboards.  

---

## Getting Started

You can explore the live platform here:  
👉 [https://syncrift.me](https://syncrift.me)

Or set it up locally by following the instructions below.  

---

## Setup Instructions

### 1. Clone the Repository
```bash
git clone https://github.com/Synthasix/syncrift.git
cd syncrift
```

---

### 2. Frontend Setup (React)

Folder: **`syncrift-frontend/`**

1. **Navigate to frontend:**
   ```bash
   cd syncrift-frontend
   ```

2. **Install dependencies:**
   ```bash
   npm install
   ```

3. **Change the backend address:**

   In the `.env` file change the VITE_SERVER to your backend address.
   If using default then change it to `http://localhost:8080`

4. **Run the development server:**
   ```bash
   npm run dev
   ```


### 3. Backend Setup (Spring Boot + Maven)

Folder: **`syncrift-backend/`**

1. **Navigate to backend:**
   ```bash
   cd syncrift-backend
   ```

2. **Initialize Maven dependencies:**
   ```bash
   mvn clean install
   ```

3. **Configure Environment Variables:**

   Inside the `src/main/resources/` folder, you will find a file named:  
   ```
   .env.properties.example
   ```

   Rename it to:
   ```
   .env.properties
   ```
   Then, fill all the given fields.


   #### How to obtain these values:
   - **jwt.secret** → Generate a random string (e.g., using [RandomKeygen](https://randomkeygen.com/) or `openssl rand -hex 32`).  
   - **cors.allowed** → Comma-separated list of frontend origins (e.g., `http://localhost:3000,https://syncrift.me`).  
   - **admin.username & admin.password** → Choose your own credentials for the backend admin panel.  
   - **db.url, db.username, db.password** → Database connection details. For local PostgreSQL:  
     ```properties
     db.url=jdbc:postgresql://localhost:5432/syncrift
     db.username=your_db_user
     db.password=your_db_password
     ```
   - **server.address & server.port** → The host/IP and port for the backend. Commonly:  
     ```properties
     server.address=localhost
     server.port=8080
     ```
   - **Cloudinary (for media storage):**  
     1. Sign up at [Cloudinary](https://cloudinary.com).  
     2. From the **Dashboard**, copy your:  
        - `cloud_name`  
        - `api_key`  
        - `api_secret`  
   - **image.upload.temp-dir** → Local temp folder for storing uploads before sending to Cloudinary.  

4. **Run the backend server:**
   ```bash
   mvn spring-boot:run
   ```

5. **Access API locally:**  
   Default URL → [http://localhost:8080](http://localhost:8080)

---

## Technologies

- **Frontend:** React, WebSockets  
- **Backend:** Spring Boot, Maven, JWT Auth  
- **Database:** PostgreSQL  
- **Cloud Storage:** Cloudinary  

---

## License

This project is licensed under the [MIT License](LICENSE).  
