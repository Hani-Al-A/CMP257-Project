// rooms.js

// Function called when user clicks "Book"
function handleBook() {
  const username = sessionStorage.getItem('username');

  if(username) {
    alert(`Hi ${username}, you can proceed to book!`);
    // Optional: redirect to booking page
    // window.location.href = 'booking.html';
  } else {
    alert('Please login first to book a room');
    window.location.href = 'login.html';
  }
}

// Update navbar login link and handle logout
document.addEventListener('DOMContentLoaded', () => {
  const username = sessionStorage.getItem('username');
  const isAdmin = sessionStorage.getItem('isAdmin') === 'true';
  const navLogin = document.getElementById('navLogin');

  if(username && navLogin) {
    // Show username and logout option
    navLogin.textContent = `Hello, ${username} (Logout)`;
    navLogin.href = '#';
    navLogin.style.cursor = 'pointer';

    navLogin.addEventListener('click', () => {
      sessionStorage.clear(); // clear session
      alert('You have been logged out.');
      window.location.href = 'index.html'; // redirect home
    });
  }

  // Optional: If you want to handle admin UI here
  if(isAdmin) {
    console.log('Admin logged in: special privileges enabled.');
    // You can enable edit/delete/add features on bookings page
  }
});
