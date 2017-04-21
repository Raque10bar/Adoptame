<?php

require_once 'include/Pets_Functions.php';
$db = new Pets_Functions();

$picture = $_POST["image"];

$result = $db->deletePet($picture);

if ($result) {
    $response["success"] = 1;
} else {
    $response["success"] = 0;
}

die(json_encode($response));
?>
