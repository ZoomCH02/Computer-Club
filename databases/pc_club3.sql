-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Хост: 127.0.0.1
-- Время создания: Дек 04 2024 г., 20:24
-- Версия сервера: 10.4.32-MariaDB-log
-- Версия PHP: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- База данных: `pc_club`
--

DELIMITER $$
--
-- Процедуры
--
CREATE DEFINER=`root`@`localhost` PROCEDURE `ConfirmReservation` (IN `p_user_id` INT, IN `p_computer_id` INT)   BEGIN
    DECLARE start_time TIMESTAMP;
    DECLARE end_time TIMESTAMP;
    DECLARE total_cost DOUBLE;

    -- Получение времени из Reservations
    SELECT reservation_time, end_time
    INTO start_time, end_time
    FROM Reservations
    WHERE user_id = p_user_id AND computer_id = p_computer_id;

    -- Проверка, найдена ли запись
    IF start_time IS NULL THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Reservation not found.';
    END IF;

    -- Расчет стоимости
    SET total_cost = TIMESTAMPDIFF(MINUTE, start_time, end_time) * 2; -- Пример $2 за минуту

    -- Вставка в Orders
    INSERT INTO Orders (user_id, computer_id, start_time, end_time, total_cost)
    VALUES (p_user_id, p_computer_id, start_time, end_time, total_cost);

    -- Удаление из Reservations
    DELETE FROM Reservations WHERE user_id = p_user_id AND computer_id = p_computer_id;
END$$

CREATE DEFINER=`user013_user1`@`localhost` PROCEDURE `GetCertificationTests` ()   BEGIN
    SELECT
        ct.TestID,
        oi.Name AS ObjectName,
        ct.TestName,
        ct.TestDate,
        ct.Status
    FROM
        Certification_Tests ct
    JOIN
        Object_Information oi ON ct.ObjectID = oi.ObjectID;
END$$

CREATE DEFINER=`user013_user1`@`localhost` PROCEDURE `GetSpecialResearch` ()   BEGIN
    SELECT
        sr.ResearchID,
        oi.Name AS ObjectName,
        sr.ResearchName,
        sr.ResearchDate,
        sr.Result
    FROM
        Special_Research sr
    JOIN
        Object_Information oi ON sr.ObjectID = oi.ObjectID;
END$$

DELIMITER ;

-- --------------------------------------------------------

--
-- Структура таблицы `computers`
--

CREATE TABLE `computers` (
  `computer_id` int(11) NOT NULL,
  `computer_name` varchar(50) NOT NULL,
  `specifications` text NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Дамп данных таблицы `computers`
--

INSERT INTO `computers` (`computer_id`, `computer_name`, `specifications`) VALUES
(1, 'PC-01', 'Intel i7, 16GB RAM, RTX 3060'),
(2, 'PC-02', 'Intel i5, 8GB RAM, GTX 1660');

-- --------------------------------------------------------

--
-- Структура таблицы `games`
--

CREATE TABLE `games` (
  `game_id` int(11) NOT NULL,
  `game_name` varchar(100) NOT NULL,
  `description` text NOT NULL,
  `genre_id` int(11) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Дамп данных таблицы `games`
--

INSERT INTO `games` (`game_id`, `game_name`, `description`, `genre_id`) VALUES
(1, 'Cyberpunk 2077', 'Ролевая игра в открытом мире', 3),
(2, 'Valorant', 'Командный шутер', 1),
(10, 'The Witcher 3: Wild Hunt', 'An open-world RPG set in a medieval fantasy world, where players control Geralt of Rivia.', 3),
(11, 'Halo Infinite', 'A sci-fi first-person shooter with multiplayer mode, part of the Halo franchise.', 1),
(12, 'Minecraft', 'A sandbox video game where players can build and explore procedurally generated worlds.', 2),
(13, 'The Legend of Zelda: Breath of the Wild', 'An adventure game where players explore a vast open world and solve puzzles.', 2),
(15, 'Fortnite', 'A multiplayer battle royale game where players compete to be the last one standing.', 2),
(16, 'Assassin\'s Creed Valhalla', 'An action-adventure game set in the Viking era, where players explore England and Norway.', 1),
(17, 'Red Dead Redemption 2', 'An open-world action-adventure game set in the American frontier during the 1890s.', 1),
(18, 'Civilization VI', 'A strategy game where players build and lead an empire from ancient times to the modern era.', 5),
(19, 'The Sims 4', 'A life simulation game where players control the lives of virtual characters, known as Sims.', 4),
(20, 'Battlefield V', 'A first-person shooter game set in World War II, with multiplayer and single-player modes.', 1),
(21, 'Call of Duty: Warzone', 'A free-to-play battle royale game in the Call of Duty universe.', 1),
(22, 'Grand Theft Auto V', 'An open-world action-adventure game where players take on the role of criminals in Los Santos.', 1),
(23, 'Stardew Valley', 'A farming simulation role-playing game where players cultivate crops, raise animals, and explore a small town.', 4),
(24, 'Among Us', 'A multiplayer party game where players work together to complete tasks while impostors try to sabotage the group.', 2),
(25, 'Dead by Daylight', 'A multiplayer horror game where survivors try to escape while being hunted by a killer.', 1),
(26, 'FIFA 22', 'A sports simulation game that focuses on football, featuring realistic graphics and gameplay mechanics.', 4),
(27, 'NBA 2K21', 'A basketball simulation game that allows players to control teams and play in real-world arenas.', 4),
(28, 'Rocket League', 'A combination of soccer and vehicular acrobatics, where players use rocket-powered cars to hit a ball into the goal.', 1),
(29, 'Overwatch', 'A team-based first-person shooter featuring a diverse cast of characters, each with unique abilities.', 1),
(30, 'Goat Simulator 3', 'Компьютерная игра в жанре action. Игра разрабатывалась студией Coffee Stain North. Является сиквелом Goat Simulator, была анонсирована на Summer Game Fest и вышла 17 ноября 2022 года.', 1);

-- --------------------------------------------------------

--
-- Структура таблицы `gamestages`
--

CREATE TABLE `gamestages` (
  `stage_id` int(11) NOT NULL,
  `game_id` int(11) NOT NULL,
  `stage_description` text NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Дамп данных таблицы `gamestages`
--

INSERT INTO `gamestages` (`stage_id`, `game_id`, `stage_description`) VALUES
(1, 1, 'Завершение пролога'),
(2, 1, 'Собрать банду для ограбления'),
(3, 2, 'Завоевать 5 побед в рейтинге');

-- --------------------------------------------------------

--
-- Структура таблицы `genres`
--

CREATE TABLE `genres` (
  `id` int(11) NOT NULL,
  `name` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Дамп данных таблицы `genres`
--

INSERT INTO `genres` (`id`, `name`) VALUES
(1, 'Action'),
(2, 'Adventure'),
(3, 'RPG'),
(4, 'Simulation'),
(5, 'Strategy'),
(6, 'Sports'),
(7, 'Puzzle');

-- --------------------------------------------------------

--
-- Структура таблицы `orders`
--

CREATE TABLE `orders` (
  `order_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `computer_id` int(11) NOT NULL,
  `start_time` datetime NOT NULL,
  `end_time` datetime NOT NULL,
  `total_cost` decimal(10,2) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Дамп данных таблицы `orders`
--

INSERT INTO `orders` (`order_id`, `user_id`, `computer_id`, `start_time`, `end_time`, `total_cost`) VALUES
(8, 2, 1, '2024-12-04 22:00:00', '2024-12-04 23:00:00', 100.00),
(9, 2, 2, '2024-12-04 22:00:00', '2024-12-04 23:00:00', 100.00),
(10, 2, 2, '2024-12-05 00:00:00', '2024-12-05 04:00:00', 400.00);

-- --------------------------------------------------------

--
-- Структура таблицы `reservations`
--

CREATE TABLE `reservations` (
  `reservation_id` int(11) NOT NULL,
  `user_id` int(11) NOT NULL,
  `computer_id` int(11) NOT NULL,
  `reservation_time` datetime NOT NULL,
  `end_time` datetime NOT NULL,
  `is_active` tinyint(1) DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Триггеры `reservations`
--
DELIMITER $$
CREATE TRIGGER `check_reservation_overlap` BEFORE INSERT ON `reservations` FOR EACH ROW BEGIN
    DECLARE overlap_count INT;

    -- Проверка пересечения в таблице Reservations
    SELECT COUNT(*) INTO overlap_count
    FROM Reservations
    WHERE computer_id = NEW.computer_id
      AND NEW.reservation_time < end_time
      AND NEW.end_time > reservation_time;

    IF overlap_count > 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'Пересечение с существующей резервацией.';
    END IF;

    -- Проверка пересечения в таблице Orders
    SELECT COUNT(*) INTO overlap_count
    FROM Orders
    WHERE computer_id = NEW.computer_id
      AND NEW.reservation_time < end_time
      AND NEW.end_time > start_time;

    IF overlap_count > 0 THEN
        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT = 'На это время ПК занят!';
    END IF;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Структура таблицы `users`
--

CREATE TABLE `users` (
  `id` int(11) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `role` enum('client','employee') DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Дамп данных таблицы `users`
--

INSERT INTO `users` (`id`, `username`, `password_hash`, `role`) VALUES
(1, 'admin', '1', 'employee'),
(2, 'user', '1', 'client'),
(3, 'client', '1', 'client'),
(4, 'tester', '1', 'client');

--
-- Индексы сохранённых таблиц
--

--
-- Индексы таблицы `computers`
--
ALTER TABLE `computers`
  ADD PRIMARY KEY (`computer_id`),
  ADD UNIQUE KEY `computer_name` (`computer_name`);

--
-- Индексы таблицы `games`
--
ALTER TABLE `games`
  ADD PRIMARY KEY (`game_id`),
  ADD UNIQUE KEY `game_name` (`game_name`),
  ADD KEY `FK_Genre` (`genre_id`);

--
-- Индексы таблицы `gamestages`
--
ALTER TABLE `gamestages`
  ADD PRIMARY KEY (`stage_id`),
  ADD KEY `game_id` (`game_id`);

--
-- Индексы таблицы `genres`
--
ALTER TABLE `genres`
  ADD PRIMARY KEY (`id`);

--
-- Индексы таблицы `orders`
--
ALTER TABLE `orders`
  ADD PRIMARY KEY (`order_id`),
  ADD KEY `user_id` (`user_id`),
  ADD KEY `computer_id` (`computer_id`);

--
-- Индексы таблицы `reservations`
--
ALTER TABLE `reservations`
  ADD PRIMARY KEY (`reservation_id`),
  ADD KEY `user_id` (`user_id`),
  ADD KEY `computer_id` (`computer_id`);

--
-- Индексы таблицы `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`id`),
  ADD UNIQUE KEY `username` (`username`);

--
-- AUTO_INCREMENT для сохранённых таблиц
--

--
-- AUTO_INCREMENT для таблицы `computers`
--
ALTER TABLE `computers`
  MODIFY `computer_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- AUTO_INCREMENT для таблицы `games`
--
ALTER TABLE `games`
  MODIFY `game_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=32;

--
-- AUTO_INCREMENT для таблицы `gamestages`
--
ALTER TABLE `gamestages`
  MODIFY `stage_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=4;

--
-- AUTO_INCREMENT для таблицы `genres`
--
ALTER TABLE `genres`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=8;

--
-- AUTO_INCREMENT для таблицы `orders`
--
ALTER TABLE `orders`
  MODIFY `order_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT для таблицы `reservations`
--
ALTER TABLE `reservations`
  MODIFY `reservation_id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=19;

--
-- AUTO_INCREMENT для таблицы `users`
--
ALTER TABLE `users`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=5;

--
-- Ограничения внешнего ключа сохраненных таблиц
--

--
-- Ограничения внешнего ключа таблицы `games`
--
ALTER TABLE `games`
  ADD CONSTRAINT `FK_Genre` FOREIGN KEY (`genre_id`) REFERENCES `genres` (`id`) ON DELETE CASCADE;

--
-- Ограничения внешнего ключа таблицы `gamestages`
--
ALTER TABLE `gamestages`
  ADD CONSTRAINT `gamestages_ibfk_1` FOREIGN KEY (`game_id`) REFERENCES `games` (`game_id`);

--
-- Ограничения внешнего ключа таблицы `orders`
--
ALTER TABLE `orders`
  ADD CONSTRAINT `orders_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `orders_ibfk_2` FOREIGN KEY (`computer_id`) REFERENCES `computers` (`computer_id`);

--
-- Ограничения внешнего ключа таблицы `reservations`
--
ALTER TABLE `reservations`
  ADD CONSTRAINT `reservations_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  ADD CONSTRAINT `reservations_ibfk_2` FOREIGN KEY (`computer_id`) REFERENCES `computers` (`computer_id`);
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
