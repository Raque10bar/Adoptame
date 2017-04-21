<?php

require_once 'include/Pets_Functions.php';
$db = new Pets_Functions();

$params = array (
    'name' => $_POST["name"],
    'sex' => $_POST["gender"],
    'size' => $_POST["size"],
    'age' => $_POST["age"],
    'weight' => $_POST["weight"],
    'species' => $_POST["species"],
    'breed' => $_POST["breed"],
    'description' => $_POST["description"],
    'imageName' => $_POST["imageName"]
);
$image = $_POST["image"];

$result = $db->editPet($params, $image);
if ($result) {
    $response["success"] = 1;
} else {
    $response["success"] = 0;
}

die(json_encode($response));
?>
