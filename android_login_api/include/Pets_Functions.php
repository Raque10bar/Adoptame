<?php

class Pets_Functions
{
    private $conn;

    // constructor
    function __construct() {
        require_once 'DB_Connect.php';
        // connecting to database
        $db = new DB_Connect();
        $this->conn = $db->connect();
    }

    // destructor
    function __destruct() {

    }

    public function getPets() {

        $stmt = $this->conn->prepare("SELECT * FROM pets");
        $result = $stmt->execute();

        $pets = array();
        if ($result) {
            $i = 0;
            while($row = $stmt->fetch(PDO::FETCH_ASSOC)){
                $pets[$i]['id'] = $row['id'];
                $pets[$i]['image'] = $row['picture'];
                $pets[$i]['name'] = $row['name'];
                $pets[$i]['gender'] = $row['gender'];
                $pets[$i]['size'] = $row['size'];
                $pets[$i]['age'] = $row['age'];
                $pets[$i]['weight'] = $row['weight'];
                $pets[$i]['species'] = $row['species'];
                $pets[$i]['breed'] = $row['breed'];
                $pets[$i]['description'] = $row['description'];
                $i = $i + 1;
            }

            if (i === 0) {
                return null;
            } else {
                return $pets;
            }
            return $pets;
        } else {
            return null;
        }
    }

    public function addPet($petData, $image) {

        $stmt = $this->conn->prepare("INSERT INTO pets (name, gender, size, age, weight, species, breed, description) VALUES (:name, :sex, :size, :age, :weight, :species, :breed, :description)");
        $result = $stmt->execute($petData);

        if ($result) {
            $lastId = $this->conn->lastInsertId();
            $photo_path = $lastId . ".JPG";

            $stmt = $this->conn->prepare("UPDATE pets SET picture= :photo_path WHERE id= :lastId");
            $stmt->bindParam(':photo_path', $photo_path);
            $stmt->bindParam(':lastId', $lastId);
            $result = $stmt->execute();

            if ($result) {
                $decodedImage = base64_decode("$image");
                file_put_contents("./pictures/" . $photo_path, $decodedImage);
                return true;
            }
        }
        return false;
    }

    public function editPet($petData, $image) {
        $stmt = $this->conn->prepare("UPDATE pets SET name = :name, gender = :sex, size = :size, age = :age, weight = :weight, species = :species, breed = :breed, description = :description WHERE picture = :imageName");
        $result = $stmt->execute($petData);
        if ($result) {
            $decodedImage = base64_decode("$image");
            unlink("../pictures/" . $petData["imageName"]);
            file_put_contents("../pictures/" . $petData["imageName"], $decodedImage);
            return true;
        }
        return false;
    }

    public function deletePet($photoName) {
        $stmt = $this->conn->prepare("DELETE FROM pets WHERE picture=:picture");
        $stmt->bindParam(':picture', $photoName);
        $result = $stmt->execute();

        if ($result) {
            $deleted = unlink("../pictures/" . $photoName);

            if ($deleted) {
                return true;
            }
        }
        return false;
    }

}
?>
