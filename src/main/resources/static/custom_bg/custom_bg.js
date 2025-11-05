/*home 화면 ui 배경색을 위한 js 파일*/

'use strict';


const mainHeader = document.getElementById('layout-navbar');

document.addEventListener("DOMContentLoaded", function () {
  const savedTheme = localStorage.getItem("color-theme") || "light";
  document.documentElement.setAttribute("color-theme", savedTheme);
});

const lightBtn = document.getElementById('light-btn');
const darkBtn = document.getElementById('dark-btn');

lightBtn.addEventListener('click', () => {      
  const currentTheme = document.documentElement.getAttribute("color-theme");
  document.documentElement.setAttribute("color-theme", "light");
  localStorage.setItem("color-theme", "light");
});

darkBtn.addEventListener('click', () => {      
  const currentTheme = document.documentElement.getAttribute("color-theme");
  document.documentElement.setAttribute("color-theme", "dark");
  localStorage.setItem("color-theme", "dark");
});















