/*home 화면 ui 테마변경을 위한 js 파일*/

'use strict';


const mainHeader = document.getElementById('layout-navbar');

document.addEventListener("DOMContentLoaded", function () {
  const savedTheme = localStorage.getItem("color-theme") || "light";
  document.documentElement.setAttribute("color-theme", savedTheme);
});

const lightBtn = document.getElementById('light-btn');
const darkBtn = document.getElementById('dark-btn');
const greenBtn = document.getElementById('green-btn');

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

greenBtn.addEventListener('click', () => {      
  const currentTheme = document.documentElement.getAttribute("color-theme");
  document.documentElement.setAttribute("color-theme", "green");
  localStorage.setItem("color-theme", "green");
});















