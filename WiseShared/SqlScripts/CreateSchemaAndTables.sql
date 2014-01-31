delimiter $$

CREATE DATABASE `wise_shared` /*!40100 DEFAULT CHARACTER SET utf8 */$$

CREATE TABLE `wise_shared`.`images` (
  `idimages` int(11) NOT NULL AUTO_INCREMENT,
  `filename` varchar(45) NOT NULL,
  `studyname` varchar(45),
  `filecontents` blob NOT NULL,
  PRIMARY KEY (`idimages`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8$$

CREATE TABLE `wise_shared`.`stylesheets` (
  `idstylesheets` int(11) NOT NULL AUTO_INCREMENT,
  `filename` varchar(45) NOT NULL,
  `studyname` varchar(45),
  `filecontents` blob NOT NULL,
  PRIMARY KEY (`idstylesheets`)
) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8$$
