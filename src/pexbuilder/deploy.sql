-- phpMyAdmin SQL Dump
-- version 4.5.4.1deb2ubuntu2
-- http://www.phpmyadmin.net
--
-- Host: localhost
-- Generation Time: Jun 27, 2017 at 03:56 AM
-- Server version: 5.7.18-0ubuntu0.16.04.1
-- PHP Version: 7.0.18-0ubuntu0.16.04.1

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `PEXBuilder`
--

-- --------------------------------------------------------

--
-- Table structure for table `permissions`
--

CREATE TABLE `permissions` (
  `id` int(11) NOT NULL,
  `name` varchar(50) NOT NULL,
  `type` tinyint(1) NOT NULL,
  `permission` mediumtext NOT NULL,
  `world` varchar(50) NOT NULL,
  `value` mediumtext NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `permissions`
--

INSERT INTO `permissions` (`id`, `name`, `type`, `permission`, `world`, `value`) VALUES
(1, 'system', 2, 'schema_version', '', '2'),
(2, 'default', 0, 'modifyworld.*', '', ''),
(3, 'default', 0, 'default', '', 'true');

-- --------------------------------------------------------

--
-- Table structure for table `permissions_entity`
--

CREATE TABLE `permissions_entity` (
  `id` int(11) NOT NULL,
  `name` varchar(50) NOT NULL,
  `type` tinyint(1) NOT NULL,
  `default` tinyint(1) NOT NULL DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Dumping data for table `permissions_entity`
--

INSERT INTO `permissions_entity` (`id`, `name`, `type`, `default`) VALUES
(1, 'default', 0, 0);

-- --------------------------------------------------------

--
-- Table structure for table `permissions_inheritance`
--

CREATE TABLE `permissions_inheritance` (
  `id` int(11) NOT NULL,
  `child` varchar(50) NOT NULL,
  `parent` varchar(50) NOT NULL,
  `type` tinyint(1) NOT NULL,
  `world` varchar(50) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `permissions`
--
ALTER TABLE `permissions`
  ADD PRIMARY KEY (`id`),
  ADD KEY `user` (`name`,`type`),
  ADD KEY `world` (`world`,`name`,`type`);

--
-- Indexes for table `permissions_entity`
--
ALTER TABLE `permissions_entity`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `name` (`name`,`type`),
  ADD KEY `default` (`default`);

--
-- Indexes for table `permissions_inheritance`
--
ALTER TABLE `permissions_inheritance`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `child` (`child`,`parent`,`type`,`world`),
  ADD KEY `child_2` (`child`,`type`),
  ADD KEY `parent` (`parent`,`type`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `permissions`
--
ALTER TABLE `permissions`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;
--
-- AUTO_INCREMENT for table `permissions_entity`
--
ALTER TABLE `permissions_entity`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;
--
-- AUTO_INCREMENT for table `permissions_inheritance`
--
ALTER TABLE `permissions_inheritance`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
