<?php

class DB_Connect {
    private $conn;

    // Connecting to database
    public function connect() {
        require_once 'include/Config.php';

        try {
          $this->conn = new PDO('mysql:host=' . DB_HOST . ';dbname=' . DB_DATABASE . ';charset=utf8', DB_USER, DB_PASSWORD);
        } catch(PDOException $ex) {
          die(json_encode(array('outcome' => false, 'message' => 'Unable to connect')));
        }
        // return database handler
        return $this->conn;
    }
}

?>
