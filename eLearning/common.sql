-- phpMyAdmin SQL Dump
-- version 3.2.0.1
-- http://www.phpmyadmin.net
--
-- Gazda: localhost
-- Timp de generare: 22 Feb 2014 la 13:00
-- Versiune server: 5.1.36
-- Versiune PHP: 5.3.0

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Baza de date: `common`
--

CREATE DATABASE IF NOT EXISTS `common` DEFAULT CHARACTER SET latin1 COLLATE latin1_swedish_ci;
USE `common`;

-- --------------------------------------------------------

--
-- Structura de tabel pentru tabelul `city`
--

CREATE TABLE IF NOT EXISTS `city` (
  `id` int(1) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 COMMENT='orasele in care se afla unitatiile de invatamant' AUTO_INCREMENT=2 ;

--
-- Salvarea datelor din tabel `city`
--

INSERT INTO `city` (`id`, `name`) VALUES
(1, 'Bucuresti');

-- --------------------------------------------------------

--
-- Structura de tabel pentru tabelul `role`
--

CREATE TABLE IF NOT EXISTS `role` (
  `id` int(1) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=5 ;

--
-- Salvarea datelor din tabel `role`
--

INSERT INTO `role` (`id`, `name`) VALUES
(1, 'administrator'),
(3, 'auxiliar'),
(4, 'elev'),
(2, 'profesor');

-- --------------------------------------------------------

--
-- Structura de tabel pentru tabelul `school`
--

CREATE TABLE IF NOT EXISTS `school` (
  `id` int(3) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `branch` varchar(200) DEFAULT NULL,
  `city` int(1) NOT NULL,
  `type` int(2) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 COMMENT='tabela cu scolile partenere' AUTO_INCREMENT=4 ;

--
-- Salvarea datelor din tabel `school`
--

INSERT INTO `school` (`id`, `name`, `branch`, `city`, `type`) VALUES
(1, 'Scoala Fictiva nr 007', NULL, 1, 1),
(2, 'Liceul Teoretic Minunea Natiunii', NULL, 1, 2),
(3, 'Universitatea de Magicienilor', 'Facultatea de Iluzionism', 1, 3);

-- --------------------------------------------------------

--
-- Structura de tabel pentru tabelul `school_type`
--

CREATE TABLE IF NOT EXISTS `school_type` (
  `id` int(1) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 COMMENT='treapta de invatamant a scolii' AUTO_INCREMENT=4 ;

--
-- Salvarea datelor din tabel `school_type`
--

INSERT INTO `school_type` (`id`, `name`) VALUES
(3, 'facultate'),
(2, 'liceu'),
(1, 'scoala');
