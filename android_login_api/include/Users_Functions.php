<?php

class Users_Functions
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

    /**
     * Check user is existed or not
     */
    public function isUserExisted($email) {

        $stmt = $this->conn->prepare("SELECT email from users WHERE email = :email");
        $stmt->bindParam(':email', $email);
        $stmt->execute();

        if ($stmt->rowCount() > 0) {
            // user existed
            return true;
        } else {
            // user not existed
            return false;
        }
    }

    public function loginUser($email, $password) {
        $login = null;
        $stmt = $this->conn->prepare("SELECT * FROM users WHERE email = :email");
        $stmt->bindParam(':email', $email);
        $result = $stmt->execute();

        if ($result) {
            $login = false;
            $user = $stmt->fetch(PDO::FETCH_ASSOC);
            $salt = $user['salt'];
            $encrypted = $user['encrypted_password'];
            $check_encrypted = $this->checkhashSSHA($salt, $password);

            if ($encrypted === $check_encrypted) {
                $login = true;
            }
        }
        return $login;
    }

    public function registerUser($email, $password) {

        if ($this->isUserExisted($email)) {
            return null;
        } else {
            $hash = $this->hashSSHA($password);
            $encrypted_password = $hash["encrypted"]; // encrypted password
            $salt = $hash["salt"]; // salt

            $stmt = $this->conn->prepare("INSERT INTO users(email, encrypted_password, salt) VALUES(:email, :encrypted, :salt)");
            $stmt->bindParam(':email', $email);
            $stmt->bindParam(':encrypted', $encrypted_password);
            $stmt->bindParam(':salt', $salt);
            $result = $stmt->execute();

            // check for successful store
            if ($result) {
                return true;
            } else {
                return false;
            }
        }
    }

    public function changePassword($email, $password) {
        $hash = $this->hashSSHA($password);
        $encrypted_password = $hash["encrypted"]; // encrypted password
        $salt = $hash["salt"]; // salt

        $stmt = $this->conn->prepare("UPDATE users SET encrypted_password = :encrypted, salt = :salt WHERE email = :email");
        $stmt->bindParam(':email', $email);
        $stmt->bindParam(':encrypted', $encrypted_password);
        $stmt->bindParam(':salt', $salt);
        $result = $stmt->execute();

        if ($result) {
            return true;
        } else {
            return false;
        }
    }

    public function resetPassword($email, $adminEmail) {
        if ($this->isUserExisted($email)) {
            $random = $this->randomStr(10);
            $msg = 'La nueva contraseña es ' . $random;

            $headerFields = array(
                "Content-Type: text/html;charset=utf-8"
            );

            if($email === "admin") {
                $sent = mail($adminEmail,"Nueva contraseña Adóptame",$msg, implode("\r\n", $headerFields));
            } else {
                $sent = mail($email,"Nueva contraseña Adóptame",$msg, implode("\r\n", $headerFields));
            }

            if ($sent) {
                $result = $this->changePassword($email, $random);
                return $result;
            } else {
                return false;
            }
        } else {
            return null;
        }
    }

    /**
     * Encrypting password
     * @param password
     * returns salt and encrypted password
     */
    public function hashSSHA($password) {
        $salt = sha1(rand());
        $salt = substr($salt, 0, 10);
        $encrypted = base64_encode(sha1($password . $salt, true) . $salt);
        $hash = array("salt" => $salt, "encrypted" => $encrypted);
        return $hash;
    }

    /**
     * Decrypting password
     * @param salt, password
     * returns encrypted password
     */
    public function checkhashSSHA($salt, $password) {
        $hash = base64_encode(sha1($password . $salt, true) . $salt);
        return $hash;
    }

    public function randomStr($length, $keyspace = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ') {
        $str = '';
        $max = mb_strlen($keyspace, '8bit') - 1;
        for ($i = 0; $i < $length; ++$i) {
            $str .= $keyspace[rand(0, $max)];
        }
        return $str;
    }
}
?>
