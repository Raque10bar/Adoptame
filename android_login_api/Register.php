<?php

require_once 'include/Users_Functions.php';
$db = new Users_Functions();

$email = $_POST["email"];
$password = $_POST["password"];

$result = $db->registerUser($email, $password);

if ($result === null) {
    $response["success"] = -1;
} else {
    if ($result) {
        $response["success"] = 1;
    } else {
        $response["success"] = 0;
    }
}
die(json_encode($response));
?>
