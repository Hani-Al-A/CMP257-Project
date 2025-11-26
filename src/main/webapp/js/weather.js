// using the open meteo free weather api to show weather in the top corner
// Dubai latitude is 25.2048 and Longitude is 55.2708
 const apiUrl = 'https://api.open-meteo.com/v1/forecast?latitude=25.2048&longitude=55.2708&current_weather=true';

function fetchWeather() {
  fetch(apiUrl)
    .then(response => response.json())
    .then(data => {
      const temp = Math.round(data.current_weather.temperature);
      
      document.getElementById('weather-temp').innerText = `${temp}°C`;
      
      // icon mapping based on weather code
      // 0=Clear, 1-3=Cloudy, 61-65=Rain, etc.
      const code = data.current_weather.weathercode;
      
      // Open meteo provides 'is_day' boolean in current_weather
      const isDay = data.current_weather.is_day === 1;

      let icon = isDay ? '☀️' : '🌙'; 
      
      if (code > 2 && code < 50) icon = isDay ? '☁️' : '☁️';
      if (code >= 50 && code < 80) icon = '🌧️'; 
      if (code >= 80) icon = '⛈️'; 
      
      if (!isDay && code <= 2) {
         icon = '✨🌙'; 
      }

      document.getElementById('weather-icon').innerText = icon;
    })
    .catch(error => {
      console.error('Weather API Error:', error);
      document.getElementById('weather-temp').innerText = 'Dubai';
    });
}

fetchWeather();
