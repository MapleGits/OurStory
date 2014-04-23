<?php

$host['naam'] = 'localhost';            // my host

$host['gebruikersnaam'] = 'root';       // my database username

$host['wachtwoord'] = '';   		// my database password

$host['databasenaam'] = '';       // my database name





$db = mysql_connect($host['naam'], $host['gebruikersnaam'], $host['wachtwoord']) OR die ('Check config.php');

mysql_select_db($host['databasenaam'], $db);	//Dont edit!

$name = $_POST['username'];


$d = 'UPDATE accounts SET loggedin = 0 WHERE name = "'.$name.'"';

mysql_query($d) OR die (mysql_error());


?>
