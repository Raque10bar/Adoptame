<?php

require_once 'include/Pets_Functions.php';
$db = new Pets_Functions();

$result = $db->getPets();

die(json_encode($result));

?>
