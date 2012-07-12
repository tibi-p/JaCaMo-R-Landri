-- phpMyAdmin SQL Dump
-- version 3.5.1
-- http://www.phpmyadmin.net
--
-- Host: 127.0.0.1
-- Generation Time: Jul 12, 2012 at 01:35 PM
-- Server version: 5.5.25a-log
-- PHP Version: 5.4.4

SET SQL_MODE="NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `rlandri`
--

-- --------------------------------------------------------

--
-- Table structure for table `auth_group`
--

CREATE TABLE IF NOT EXISTS `auth_group` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(80) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Table structure for table `auth_group_permissions`
--

CREATE TABLE IF NOT EXISTS `auth_group_permissions` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `group_id` int(11) NOT NULL,
  `permission_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `group_id` (`group_id`,`permission_id`),
  KEY `auth_group_permissions_425ae3c4` (`group_id`),
  KEY `auth_group_permissions_1e014c8f` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Table structure for table `auth_permission`
--

CREATE TABLE IF NOT EXISTS `auth_permission` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `content_type_id` int(11) NOT NULL,
  `codename` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `content_type_id` (`content_type_id`,`codename`),
  KEY `auth_permission_1bb8f392` (`content_type_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=61 ;

--
-- Dumping data for table `auth_permission`
--

INSERT INTO `auth_permission` (`id`, `name`, `content_type_id`, `codename`) VALUES
(1, 'Can add permission', 1, 'add_permission'),
(2, 'Can change permission', 1, 'change_permission'),
(3, 'Can delete permission', 1, 'delete_permission'),
(4, 'Can add group', 2, 'add_group'),
(5, 'Can change group', 2, 'change_group'),
(6, 'Can delete group', 2, 'delete_group'),
(7, 'Can add user', 3, 'add_user'),
(8, 'Can change user', 3, 'change_user'),
(9, 'Can delete user', 3, 'delete_user'),
(10, 'Can add content type', 4, 'add_contenttype'),
(11, 'Can change content type', 4, 'change_contenttype'),
(12, 'Can delete content type', 4, 'delete_contenttype'),
(13, 'Can add session', 5, 'add_session'),
(14, 'Can change session', 5, 'change_session'),
(15, 'Can delete session', 5, 'delete_session'),
(16, 'Can add site', 6, 'add_site'),
(17, 'Can change site', 6, 'change_site'),
(18, 'Can delete site', 6, 'delete_site'),
(19, 'Can add log entry', 7, 'add_logentry'),
(20, 'Can change log entry', 7, 'change_logentry'),
(21, 'Can delete log entry', 7, 'delete_logentry'),
(22, 'Can add city', 8, 'add_city'),
(23, 'Can change city', 8, 'change_city'),
(24, 'Can delete city', 8, 'delete_city'),
(25, 'Can add ring', 9, 'add_ring'),
(26, 'Can change ring', 9, 'change_ring'),
(27, 'Can delete ring', 9, 'delete_ring'),
(28, 'Can add sub environment', 10, 'add_subenvironment'),
(29, 'Can change sub environment', 10, 'change_subenvironment'),
(30, 'Can delete sub environment', 10, 'delete_subenvironment'),
(31, 'Can add owner relationship', 11, 'add_ownerrelationship'),
(32, 'Can change owner relationship', 11, 'change_ownerrelationship'),
(33, 'Can delete owner relationship', 11, 'delete_ownerrelationship'),
(34, 'Can add default extra', 12, 'add_defaultextra'),
(35, 'Can change default extra', 12, 'change_defaultextra'),
(36, 'Can delete default extra', 12, 'delete_defaultextra'),
(37, 'Can add agent', 13, 'add_agent'),
(38, 'Can change agent', 13, 'change_agent'),
(39, 'Can delete agent', 13, 'delete_agent'),
(40, 'Can add artifact', 14, 'add_artifact'),
(41, 'Can change artifact', 14, 'change_artifact'),
(42, 'Can delete artifact', 14, 'delete_artifact'),
(43, 'Can add organization', 15, 'add_organization'),
(44, 'Can change organization', 15, 'change_organization'),
(45, 'Can delete organization', 15, 'delete_organization'),
(46, 'Can add env user', 16, 'add_envuser'),
(47, 'Can change env user', 16, 'change_envuser'),
(48, 'Can delete env user', 16, 'delete_envuser'),
(49, 'Can add env agent', 17, 'add_envagent'),
(50, 'Can change env agent', 17, 'change_envagent'),
(51, 'Can delete env agent', 17, 'delete_envagent'),
(52, 'Can add solution', 18, 'add_solution'),
(53, 'Can change solution', 18, 'change_solution'),
(54, 'Can delete solution', 18, 'delete_solution'),
(55, 'Can add schedule', 19, 'add_schedule'),
(56, 'Can change schedule', 19, 'change_schedule'),
(57, 'Can delete schedule', 19, 'delete_schedule'),
(58, 'Can add abstract process', 20, 'add_abstractprocess'),
(59, 'Can change abstract process', 20, 'change_abstractprocess'),
(60, 'Can delete abstract process', 20, 'delete_abstractprocess');

-- --------------------------------------------------------

--
-- Table structure for table `auth_user`
--

CREATE TABLE IF NOT EXISTS `auth_user` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `username` varchar(30) NOT NULL,
  `first_name` varchar(30) NOT NULL,
  `last_name` varchar(30) NOT NULL,
  `email` varchar(75) NOT NULL,
  `password` varchar(128) NOT NULL,
  `is_staff` tinyint(1) NOT NULL,
  `is_active` tinyint(1) NOT NULL,
  `is_superuser` tinyint(1) NOT NULL,
  `last_login` datetime NOT NULL,
  `date_joined` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=5 ;

--
-- Dumping data for table `auth_user`
--

INSERT INTO `auth_user` (`id`, `username`, `first_name`, `last_name`, `email`, `password`, `is_staff`, `is_active`, `is_superuser`, `last_login`, `date_joined`) VALUES
(1, 'admin', '', '', 'popa.tiberiu@gmail.com', 'pbkdf2_sha256$10000$Pel4TDYzKAof$zawDS1r9uzj0ZkO/N17caDtFPsDFwcLo9unbfJVXTlU=', 1, 1, 1, '2012-07-12 11:29:01', '2012-07-12 11:13:16'),
(2, 'tibi', '', '', '', 'pbkdf2_sha256$10000$j4VqrRFjDGoy$mMDRAp9YUBpIDZXDoQDBJLe4ZhNQilyHnU7f4LUqIt4=', 0, 1, 0, '2012-07-12 11:32:02', '2012-07-12 11:14:25'),
(3, 'andrei', '', '', '', 'pbkdf2_sha256$10000$WDjG5qD6agbH$GtnTKn45ca2zlUa1uSEgtGolmWIzJ/9+iqhTaUZ86z8=', 0, 1, 0, '2012-07-12 11:14:40', '2012-07-12 11:14:40'),
(4, 'mihai', '', '', '', 'pbkdf2_sha256$10000$Ac6AXMUmTKvR$EyJJuVXpvAAq/6hPqgufSiTLweRfKQPSvoJHuvVDVbA=', 0, 1, 0, '2012-07-12 11:14:46', '2012-07-12 11:14:46');

-- --------------------------------------------------------

--
-- Table structure for table `auth_user_groups`
--

CREATE TABLE IF NOT EXISTS `auth_user_groups` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `group_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id` (`user_id`,`group_id`),
  KEY `auth_user_groups_403f60f` (`user_id`),
  KEY `auth_user_groups_425ae3c4` (`group_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Table structure for table `auth_user_user_permissions`
--

CREATE TABLE IF NOT EXISTS `auth_user_user_permissions` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` int(11) NOT NULL,
  `permission_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id` (`user_id`,`permission_id`),
  KEY `auth_user_user_permissions_403f60f` (`user_id`),
  KEY `auth_user_user_permissions_1e014c8f` (`permission_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Table structure for table `city_city`
--

CREATE TABLE IF NOT EXISTS `city_city` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(200) NOT NULL,
  `description` longtext NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=2 ;

--
-- Dumping data for table `city_city`
--

INSERT INTO `city_city` (`id`, `name`, `description`) VALUES
(1, 'The White City of R''Landri', 'It''s a city. Its purity is represented by its white color.');

-- --------------------------------------------------------

--
-- Table structure for table `city_ring`
--

CREATE TABLE IF NOT EXISTS `city_ring` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `city_id` int(11) NOT NULL,
  `index` int(10) unsigned NOT NULL,
  `size` int(10) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  KEY `city_ring_586a73b5` (`city_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=4 ;

--
-- Dumping data for table `city_ring`
--

INSERT INTO `city_ring` (`id`, `city_id`, `index`, `size`) VALUES
(1, 1, 0, 1),
(2, 1, 1, 4),
(3, 1, 2, 16);

-- --------------------------------------------------------

--
-- Table structure for table `django_admin_log`
--

CREATE TABLE IF NOT EXISTS `django_admin_log` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `action_time` datetime NOT NULL,
  `user_id` int(11) NOT NULL,
  `content_type_id` int(11) DEFAULT NULL,
  `object_id` longtext,
  `object_repr` varchar(200) NOT NULL,
  `action_flag` smallint(5) unsigned NOT NULL,
  `change_message` longtext NOT NULL,
  PRIMARY KEY (`id`),
  KEY `django_admin_log_403f60f` (`user_id`),
  KEY `django_admin_log_1bb8f392` (`content_type_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=20 ;

--
-- Dumping data for table `django_admin_log`
--

INSERT INTO `django_admin_log` (`id`, `action_time`, `user_id`, `content_type_id`, `object_id`, `object_repr`, `action_flag`, `change_message`) VALUES
(1, '2012-07-12 11:14:25', 1, 3, '2', 'tibi', 1, ''),
(2, '2012-07-12 11:14:40', 1, 3, '3', 'andrei', 1, ''),
(3, '2012-07-12 11:14:46', 1, 3, '4', 'mihai', 1, ''),
(4, '2012-07-12 11:17:44', 1, 8, '1', 'The White City of R''Landri', 1, ''),
(5, '2012-07-12 11:19:42', 1, 10, '1', 'Roulette', 1, ''),
(6, '2012-07-12 11:24:02', 1, 16, '1', 'admin', 3, ''),
(7, '2012-07-12 11:25:42', 1, 17, '1', 'Chivu (tibi)', 1, ''),
(8, '2012-07-12 11:25:48', 1, 17, '2', 'Mutu (tibi)', 1, ''),
(9, '2012-07-12 11:26:03', 1, 17, '3', 'Pacyno (andrei)', 1, ''),
(10, '2012-07-12 11:26:34', 1, 17, '4', 'Obama (andrei)', 1, ''),
(11, '2012-07-12 11:27:02', 1, 17, '5', 'Kovesi (andrei)', 1, ''),
(12, '2012-07-12 11:27:23', 1, 17, '6', 'Carlos (mihai)', 1, ''),
(13, '2012-07-12 11:27:34', 1, 17, '7', 'Gigel (mihai)', 1, ''),
(14, '2012-07-12 11:27:42', 1, 17, '8', 'Gogu (mihai)', 1, ''),
(15, '2012-07-12 11:30:48', 1, 12, '1', 'Generic Artifacts', 1, ''),
(16, '2012-07-12 11:30:49', 1, 12, '2', 'Generic Artifacts', 1, ''),
(17, '2012-07-12 11:30:57', 1, 12, '2', 'Generic Artifacts', 3, ''),
(18, '2012-07-12 11:31:10', 1, 12, '3', 'Logging Properties', 1, ''),
(19, '2012-07-12 11:31:31', 1, 12, '4', 'Generic Environment', 1, '');

-- --------------------------------------------------------

--
-- Table structure for table `django_content_type`
--

CREATE TABLE IF NOT EXISTS `django_content_type` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `app_label` varchar(100) NOT NULL,
  `model` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `app_label` (`app_label`,`model`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=21 ;

--
-- Dumping data for table `django_content_type`
--

INSERT INTO `django_content_type` (`id`, `name`, `app_label`, `model`) VALUES
(1, 'permission', 'auth', 'permission'),
(2, 'group', 'auth', 'group'),
(3, 'user', 'auth', 'user'),
(4, 'content type', 'contenttypes', 'contenttype'),
(5, 'session', 'sessions', 'session'),
(6, 'site', 'sites', 'site'),
(7, 'log entry', 'admin', 'logentry'),
(8, 'city', 'city', 'city'),
(9, 'ring', 'city', 'ring'),
(10, 'sub environment', 'subenvironment', 'subenvironment'),
(11, 'owner relationship', 'subenvironment', 'ownerrelationship'),
(12, 'default extra', 'subenvironment', 'defaultextra'),
(13, 'agent', 'subenvironment', 'agent'),
(14, 'artifact', 'subenvironment', 'artifact'),
(15, 'organization', 'subenvironment', 'organization'),
(16, 'env user', 'envuser', 'envuser'),
(17, 'env agent', 'envuser', 'envagent'),
(18, 'solution', 'solution', 'solution'),
(19, 'schedule', 'schedule', 'schedule'),
(20, 'abstract process', 'simulator', 'abstractprocess');

-- --------------------------------------------------------

--
-- Table structure for table `django_session`
--

CREATE TABLE IF NOT EXISTS `django_session` (
  `session_key` varchar(40) NOT NULL,
  `session_data` longtext NOT NULL,
  `expire_date` datetime NOT NULL,
  PRIMARY KEY (`session_key`),
  KEY `django_session_3da3d3d8` (`expire_date`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `django_session`
--

INSERT INTO `django_session` (`session_key`, `session_data`, `expire_date`) VALUES
('aa0acfc5f000712da1dd74a6cdd2fece', 'NmJmZWUyZWExMGEyNzU1NGUwZjg2MzhiNzA4ZjA3MDI5YTg0MzNiYjqAAn1xAShVEl9hdXRoX3Vz\nZXJfYmFja2VuZHECVSlkamFuZ28uY29udHJpYi5hdXRoLmJhY2tlbmRzLk1vZGVsQmFja2VuZHED\nVQ1fYXV0aF91c2VyX2lkcQSKAQJ1Lg==\n', '2012-07-26 11:32:02');

-- --------------------------------------------------------

--
-- Table structure for table `django_site`
--

CREATE TABLE IF NOT EXISTS `django_site` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `domain` varchar(100) NOT NULL,
  `name` varchar(50) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=2 ;

--
-- Dumping data for table `django_site`
--

INSERT INTO `django_site` (`id`, `domain`, `name`) VALUES
(1, 'example.com', 'example.com');

-- --------------------------------------------------------

--
-- Table structure for table `envuser_envagent`
--

CREATE TABLE IF NOT EXISTS `envuser_envagent` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(200) NOT NULL,
  `envUser_id` int(11) NOT NULL,
  `location_id` int(11) NOT NULL,
  `timePool` int(10) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  KEY `envuser_envagent_75b32dfd` (`envUser_id`),
  KEY `envuser_envagent_319d859` (`location_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=9 ;

--
-- Dumping data for table `envuser_envagent`
--

INSERT INTO `envuser_envagent` (`id`, `name`, `envUser_id`, `location_id`, `timePool`) VALUES
(1, 'Chivu', 2, 1, 272),
(2, 'Mutu', 2, 1, 272),
(3, 'Pacyno', 3, 1, 272),
(4, 'Obama', 3, 1, 272),
(5, 'Kovesi', 3, 1, 272),
(6, 'Carlos', 4, 1, 272),
(7, 'Gigel', 4, 1, 272),
(8, 'Gogu', 4, 1, 272);

-- --------------------------------------------------------

--
-- Table structure for table `envuser_envuser`
--

CREATE TABLE IF NOT EXISTS `envuser_envuser` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `rank` bigint(20) NOT NULL,
  `economy` bigint(20) NOT NULL,
  `user_id` int(11) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `user_id` (`user_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=5 ;

--
-- Dumping data for table `envuser_envuser`
--

INSERT INTO `envuser_envuser` (`id`, `rank`, `economy`, `user_id`) VALUES
(2, 0, 0, 2),
(3, 0, 0, 3),
(4, 0, 0, 4);

-- --------------------------------------------------------

--
-- Table structure for table `schedule_schedule`
--

CREATE TABLE IF NOT EXISTS `schedule_schedule` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `solution_id` int(11) NOT NULL,
  `turn` int(10) unsigned NOT NULL,
  `step` int(10) unsigned NOT NULL,
  `lastModified` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `solution_id` (`solution_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Table structure for table `simulator_abstractprocess`
--

CREATE TABLE IF NOT EXISTS `simulator_abstractprocess` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `solution_id` int(11) NOT NULL,
  `created` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `simulator_abstractprocess_6c4d8baf` (`solution_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=3 ;

--
-- Dumping data for table `simulator_abstractprocess`
--

INSERT INTO `simulator_abstractprocess` (`id`, `solution_id`, `created`) VALUES
(2, 1, '2012-07-12 11:32:05');

-- --------------------------------------------------------

--
-- Table structure for table `solution_solution`
--

CREATE TABLE IF NOT EXISTS `solution_solution` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(200) NOT NULL,
  `envUser_id` int(11) NOT NULL,
  `subEnvironment_id` int(11) NOT NULL,
  `description` longtext NOT NULL,
  `isVisible` tinyint(1) NOT NULL,
  `agents` varchar(100) NOT NULL,
  `artifacts` varchar(100) NOT NULL,
  `organizations` varchar(100) NOT NULL,
  `lastModified` datetime NOT NULL,
  PRIMARY KEY (`id`),
  KEY `solution_solution_75b32dfd` (`envUser_id`),
  KEY `solution_solution_e004a33` (`subEnvironment_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=2 ;

--
-- Dumping data for table `solution_solution`
--

INSERT INTO `solution_solution` (`id`, `name`, `envUser_id`, `subEnvironment_id`, `description`, `isVisible`, `agents`, `artifacts`, `organizations`, `lastModified`) VALUES
(1, 'Gambler', 2, 1, 'You win some, lose some, it''s all the same to me!', 1, 'users/2/solutions/1/gambler.zip', '', '', '2012-07-12 11:25:15');

-- --------------------------------------------------------

--
-- Table structure for table `subenvironment_agent`
--

CREATE TABLE IF NOT EXISTS `subenvironment_agent` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(200) NOT NULL,
  `subenvironment_id` int(11) NOT NULL,
  `file` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `subenvironment_agent_4d5c45ed` (`subenvironment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Table structure for table `subenvironment_artifact`
--

CREATE TABLE IF NOT EXISTS `subenvironment_artifact` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(200) NOT NULL,
  `subenvironment_id` int(11) NOT NULL,
  `file` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `subenvironment_artifact_4d5c45ed` (`subenvironment_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=2 ;

--
-- Dumping data for table `subenvironment_artifact`
--

INSERT INTO `subenvironment_artifact` (`id`, `name`, `subenvironment_id`, `file`) VALUES
(1, 'Biased Artifacts', 1, 'subenvironments/1/artifacts/roulette_artifacts.zip');

-- --------------------------------------------------------

--
-- Table structure for table `subenvironment_defaultextra`
--

CREATE TABLE IF NOT EXISTS `subenvironment_defaultextra` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(200) NOT NULL,
  `file` varchar(100) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=5 ;

--
-- Dumping data for table `subenvironment_defaultextra`
--

INSERT INTO `subenvironment_defaultextra` (`id`, `name`, `file`) VALUES
(1, 'Generic Artifacts', 'subenvironments/generic/extra/generic-artifacts.jar'),
(3, 'Logging Properties', 'subenvironments/generic/extra/logging.zip'),
(4, 'Generic Environment', 'subenvironments/generic/extra/Generic Environment.zip');

-- --------------------------------------------------------

--
-- Table structure for table `subenvironment_organization`
--

CREATE TABLE IF NOT EXISTS `subenvironment_organization` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(200) NOT NULL,
  `subenvironment_id` int(11) NOT NULL,
  `file` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `subenvironment_organization_4d5c45ed` (`subenvironment_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;

-- --------------------------------------------------------

--
-- Table structure for table `subenvironment_ownerrelationship`
--

CREATE TABLE IF NOT EXISTS `subenvironment_ownerrelationship` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `subEnvironment_id` int(11) NOT NULL,
  `envUser_id` int(11) NOT NULL,
  `shares` int(10) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  KEY `subenvironment_ownerrelationship_e004a33` (`subEnvironment_id`),
  KEY `subenvironment_ownerrelationship_75b32dfd` (`envUser_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=2 ;

--
-- Dumping data for table `subenvironment_ownerrelationship`
--

INSERT INTO `subenvironment_ownerrelationship` (`id`, `subEnvironment_id`, `envUser_id`, `shares`) VALUES
(1, 1, 3, 300);

-- --------------------------------------------------------

--
-- Table structure for table `subenvironment_subenvironment`
--

CREATE TABLE IF NOT EXISTS `subenvironment_subenvironment` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `name` varchar(200) NOT NULL,
  `description` longtext NOT NULL,
  `ring_id` int(11) NOT NULL,
  `index` int(10) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  KEY `subenvironment_subenvironment_77550576` (`ring_id`)
) ENGINE=InnoDB  DEFAULT CHARSET=latin1 AUTO_INCREMENT=2 ;

--
-- Dumping data for table `subenvironment_subenvironment`
--

INSERT INTO `subenvironment_subenvironment` (`id`, `name`, `description`, `ring_id`, `index`) VALUES
(1, 'Roulette', 'European Roulette', 2, 1);

--
-- Constraints for dumped tables
--

--
-- Constraints for table `auth_group_permissions`
--
ALTER TABLE `auth_group_permissions`
  ADD CONSTRAINT `group_id_refs_id_3cea63fe` FOREIGN KEY (`group_id`) REFERENCES `auth_group` (`id`),
  ADD CONSTRAINT `permission_id_refs_id_5886d21f` FOREIGN KEY (`permission_id`) REFERENCES `auth_permission` (`id`);

--
-- Constraints for table `auth_permission`
--
ALTER TABLE `auth_permission`
  ADD CONSTRAINT `content_type_id_refs_id_728de91f` FOREIGN KEY (`content_type_id`) REFERENCES `django_content_type` (`id`);

--
-- Constraints for table `auth_user_groups`
--
ALTER TABLE `auth_user_groups`
  ADD CONSTRAINT `user_id_refs_id_7ceef80f` FOREIGN KEY (`user_id`) REFERENCES `auth_user` (`id`),
  ADD CONSTRAINT `group_id_refs_id_f116770` FOREIGN KEY (`group_id`) REFERENCES `auth_group` (`id`);

--
-- Constraints for table `auth_user_user_permissions`
--
ALTER TABLE `auth_user_user_permissions`
  ADD CONSTRAINT `user_id_refs_id_dfbab7d` FOREIGN KEY (`user_id`) REFERENCES `auth_user` (`id`),
  ADD CONSTRAINT `permission_id_refs_id_67e79cb` FOREIGN KEY (`permission_id`) REFERENCES `auth_permission` (`id`);

--
-- Constraints for table `city_ring`
--
ALTER TABLE `city_ring`
  ADD CONSTRAINT `city_id_refs_id_1da26b18` FOREIGN KEY (`city_id`) REFERENCES `city_city` (`id`);

--
-- Constraints for table `django_admin_log`
--
ALTER TABLE `django_admin_log`
  ADD CONSTRAINT `user_id_refs_id_c8665aa` FOREIGN KEY (`user_id`) REFERENCES `auth_user` (`id`),
  ADD CONSTRAINT `content_type_id_refs_id_288599e6` FOREIGN KEY (`content_type_id`) REFERENCES `django_content_type` (`id`);

--
-- Constraints for table `envuser_envagent`
--
ALTER TABLE `envuser_envagent`
  ADD CONSTRAINT `envUser_id_refs_id_215320fa` FOREIGN KEY (`envUser_id`) REFERENCES `envuser_envuser` (`id`),
  ADD CONSTRAINT `location_id_refs_id_3357fab0` FOREIGN KEY (`location_id`) REFERENCES `subenvironment_subenvironment` (`id`);

--
-- Constraints for table `envuser_envuser`
--
ALTER TABLE `envuser_envuser`
  ADD CONSTRAINT `user_id_refs_id_48bb7f82` FOREIGN KEY (`user_id`) REFERENCES `auth_user` (`id`);

--
-- Constraints for table `schedule_schedule`
--
ALTER TABLE `schedule_schedule`
  ADD CONSTRAINT `solution_id_refs_id_5cb40c7` FOREIGN KEY (`solution_id`) REFERENCES `solution_solution` (`id`);

--
-- Constraints for table `simulator_abstractprocess`
--
ALTER TABLE `simulator_abstractprocess`
  ADD CONSTRAINT `solution_id_refs_id_68a5c410` FOREIGN KEY (`solution_id`) REFERENCES `solution_solution` (`id`);

--
-- Constraints for table `solution_solution`
--
ALTER TABLE `solution_solution`
  ADD CONSTRAINT `envUser_id_refs_id_7d140cb` FOREIGN KEY (`envUser_id`) REFERENCES `envuser_envuser` (`id`),
  ADD CONSTRAINT `subEnvironment_id_refs_id_110e3a39` FOREIGN KEY (`subEnvironment_id`) REFERENCES `subenvironment_subenvironment` (`id`);

--
-- Constraints for table `subenvironment_agent`
--
ALTER TABLE `subenvironment_agent`
  ADD CONSTRAINT `subenvironment_id_refs_id_2e3d8414` FOREIGN KEY (`subenvironment_id`) REFERENCES `subenvironment_subenvironment` (`id`);

--
-- Constraints for table `subenvironment_artifact`
--
ALTER TABLE `subenvironment_artifact`
  ADD CONSTRAINT `subenvironment_id_refs_id_77311c2e` FOREIGN KEY (`subenvironment_id`) REFERENCES `subenvironment_subenvironment` (`id`);

--
-- Constraints for table `subenvironment_organization`
--
ALTER TABLE `subenvironment_organization`
  ADD CONSTRAINT `subenvironment_id_refs_id_3ff55ced` FOREIGN KEY (`subenvironment_id`) REFERENCES `subenvironment_subenvironment` (`id`);

--
-- Constraints for table `subenvironment_ownerrelationship`
--
ALTER TABLE `subenvironment_ownerrelationship`
  ADD CONSTRAINT `envUser_id_refs_id_13a65d2` FOREIGN KEY (`envUser_id`) REFERENCES `envuser_envuser` (`id`),
  ADD CONSTRAINT `subEnvironment_id_refs_id_51b8c39c` FOREIGN KEY (`subEnvironment_id`) REFERENCES `subenvironment_subenvironment` (`id`);

--
-- Constraints for table `subenvironment_subenvironment`
--
ALTER TABLE `subenvironment_subenvironment`
  ADD CONSTRAINT `ring_id_refs_id_55709796` FOREIGN KEY (`ring_id`) REFERENCES `city_ring` (`id`);

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
