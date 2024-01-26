INSERT INTO users(email, name, password, roles) VALUES 
('holita@gmail.com', 'admin1', '$2a$12$k8J.LRiyIVY9cWiVXWG3xOrDrxCZyNxppjQS99Ew5BRSo8QKAjLze', 'ROLE_ADMIN'),
('hola@gmail.com', 'player1', '$2a$12$jExPzKwt6E656bpZ0taMQ.w9jsteUbbDZ9wOLqTeIXvRLy4SNIfc.', 'ROLE_USER'),
('test2@test.com', 'player2', '$2a$10$Gsic7B3Si21dACLSoIupy.QdYyDxAABc8PZaE2KIZrU77h7Zhhwxq', 'ROLE_USER'),
('test3@test.com', 'player3', '$2a$10$Gsic7B3Si21dACLSoIupy.QdYyDxAABc8PZaE2KIZrU77h7Zhhwxq', 'ROLE_USER'),
('test4@test.com', 'player4', '$2a$10$Gsic7B3Si21dACLSoIupy.QdYyDxAABc8PZaE2KIZrU77h7Zhhwxq', 'ROLE_USER'),
('test5@test.com', 'player5', '$2a$10$Gsic7B3Si21dACLSoIupy.QdYyDxAABc8PZaE2KIZrU77h7Zhhwxq', 'ROLE_USER');

INSERT INTO games(round, turn, num_players, host, mode, time, is_active) VALUES
(2, 'player5', 2, 'player2', 0, '03:24', false),
(3, 'Robot Pirate', 1, 'player4', 1, '05:49', false),
(1, 'player3', 2, 'player1', 2, '04:31', false);

INSERT INTO players(points, user_id, is_host, is_in_game, game_id) VALUES
(0, 2, false, false, 3),
(0, 3, false, false, 1),
(0, 4, false, false, 3),
(0, 5, false, false, 2),
(0, 6, false, false, 1);

INSERT INTO players_friends(player_id, friends_id) VALUES
(2, 3),
(2, 4),
(2, 5),
(3, 2),
(3, 4),
(3, 5),
(4, 2),
(4, 3),
(4, 5),
(5, 2),
(5, 3),
(5, 4);

INSERT INTO cards(name, description, card_value, type, category, is_treasure, is_protected, is_destroyed, is_chosen, is_revealed, is_played_as_permanent, is_updated_pirate_code, is_updated_first_mate, is_updated_pirate_king) VALUES 
('DavyJones',
'At the start of your turn, you may discard a card to destroy a card. At the end of your turn, draw a card.',
'Solo', 0, 0, false, false, false, false, false, false, false, false, false),
('RobotPirate',
'1) Play the rightmost card of the display for its ability. 2) Play the top card at the deck as a treasure. 3) Refill the display from the left.',
'Solo', 0, 0, false, false, false, false, false, false, false, false, false),
('AnneBonny',
'Draw 3 cards. Everyone else draws 1 card.',
'Joker', 0, 0, false, false, false, false, false, false, false, false, false),
('HenryMorgan',
'Draw 5 cards. Add 1 to your hand and put the others back in any order.',
'Joker', 0, 0, false, false, false, false, false, false, false, false, false),
('LongJohnSilver',
'Swap hands with an opponent.',
'Joker', 0, 0, false, false, false, false, false, false, false, false, false),
('MadameChing',
'Play 1 card as a permanent and 1 card as a treasure.',
'Joker', 0, 0, false, false, false, false, false, false, false, false, false),
('Stowaway',
'Play this in front of another player. Their lowest-valued treasure has no value.',
'Joker', 0, 0, false, false, false, false, false, false, false, false, false),
('Maelstrom',
'Destroy all treasures.',
'A', 0, 3, false, false, false, false, false, false, false, false, false),
('Mutiny',
'Choose a number. Destroy all treasures of that value or lower.',
'A', 0, 1, false, false, false, false, false, false, false, false, false),
('ShiverMeTimbers',
'Everyone discards all odd-numbered cards and destroys all even-numbered cards.',
'A', 0, 2, false, false, false, false, false, false, false, false, false),
('Lifeboat',
'You may destroy this card instead of destroying or discarding other cards.',
'1', 1, 1, true, false, false, false, false, false, false, false, false),
('JollyRoger',
'At the start of your turn, draw a card. If it is a treasure, return it.',
'1', 1, 2, true, false, false, false, false, false, false, false, false),
('ShipsWheel',
'Whenever an opponent draws a card as their action, draw a card.',
'1', 1, 3, true, false, false, false, false, false, false, false, false),
('CutlassEyepatch',
'Destroy a permanent card. Draw a card.',
'2', 0, 1, true, false, false, false, false, false, false, false, false),
('CutlassSkull',
'Destroy a permanent card. Draw a card.',
'2', 0, 2, true, false, false, false, false, false, false, false, false),
('CutlassHook',
'Destroy a permanent card. Draw a card.',
'2', 0, 3, true, false, false, false, false, false, false, false, false),
('MonkeyEyepatch',
'Choose a card from the discard pile and add it to your hand.',
'3', 0, 1, true, false, false, false, false, false, false, false, false),
('MonkeySkull',
'Choose a card from the discard pile and add it to your hand.',
'3', 0, 2, true, false, false, false, false, false, false, false, false),
('MonkeyHook',
'Choose a card from the discard pile and add it to your hand.',
'3', 0, 3, true, false, false, false, false, false, false, false, false),
('CannonEyepatch',
'One opponent discards 2 cards.',
'4', 0, 1, true, false, false, false, false, false, false, false, false),
('CannonSkull',
'One opponent discards 2 cards.',
'4', 0, 2, true, false, false, false, false, false, false, false, false),
('CannonHook',
'One opponent discards 2 cards.',
'4', 0, 3, true, false, false, false, false, false, false, false, false),
('TreasureMapEyepatch',
'Draw 2 cards.',
'5', 0, 1, true, false, false, false, false, false, false, false, false),
('TreasureMapSkull',
'Draw 2 cards.',
'5', 0, 2, true, false, false, false, false, false, false, false, false),
('TreasureMapHook',
'Draw 2 cards.',
'5', 0, 3, true, false, false, false, false, false, false, false, false),
('KrakenEyepatch',
'Destroy all permanents.',
'6', 0, 1, true, false, false, false, false, false, false, false, false),
('KrakenSkull',
'Destroy all permanents.',
'6', 0, 2, true, false, false, false, false, false, false, false, false),
('KrakenHook',
'Destroy all permanents.',
'6', 0, 3, true, false, false, false, false, false, false, false, false),
('SkeletonKeyEyepatch',
'Draw a card. Play a card.',
'7', 0, 1, true, false, false, false, false, false, false, false, false),
('SkeletonKeySkull',
'Draw a card. Play a card.',
'7', 0, 2, true, false, false, false, false, false, false, false, false),
('SkeletonKeyHook',
'Draw a card. Play a card.',
'7', 0, 3, true, false, false, false, false, false, false, false, false),
('SpyglassEyepatch',
'Opponents play with their hands revealed.',
'8', 1, 1, true, false, false, false, false, false, false, false, false),
('SpyglassSkull',
'Opponents play with their hands revealed.',
'8', 1, 2, true, false, false, false, false, false, false, false, false),
('SpyglassHook',
'Opponents play with their hands revealed.',
'8', 1, 3, true, false, false, false, false, false, false, false, false),
('PirateCodeEyepatch',
'Place this on top of a treasure; it is now protected. It cannot be destroyed or stolen.',
'9', 1, 1, true, false, false, false, false, false, false, false, false),
('PirateCodeSkull',
'Place this on top of a treasure; it is now protected. It cannot be destroyed or stolen.',
'9', 1, 2, true, false, false, false, false, false, false, false, false),
('PirateCodeHook',
'Place this on top of a treasure; it is now protected. It cannot be destroyed or stolen.',
'9', 1, 3, true, false, false, false, false, false, false, false, false),
('TreasureChestEyepatch',
'',
'10', 0, 1, true, false, false, false, false, false, false, false, false),
('TreasureChestSkull',
'',
'10', 0, 2, true, false, false, false, false, false, false, false, false),
('TreasureChestHook',
'',
'10', 0, 3, true, false, false, false, false, false, false, false, false),
('LookoutEyepatch',
'Steal a treasure and move it in front of you.',
'J', 0, 1, false, false, false, false, false, false, false, false, false),
('LookoutSkull',
'Steal a treasure and move it in front of you.',
'J', 0, 2, false, false, false, false, false, false, false, false, false),
('LookoutHook',
'Steal a treasure and move it in front of you.',
'J', 0, 3, false, false, false, false, false, false, false, false, false),
('FirstMateEyepatch',
'Your treasures are protected. They cannot be destroyed or stolen.',
'Q', 1, 1, false, false, false, false, false, false, false, false, false),
('FirstMateSkull',
'Your treasures are protected. They cannot be destroyed or stolen.',
'Q', 1, 2, false, false, false, false, false, false, false, false, false),
('FirstMateHook',
'Your treasures are protected. They cannot be destroyed or stolen.',
'Q', 1, 3, false, false, false, false, false, false, false, false, false),
('PirateKingEyepatch',
'Your treasures are worth 3 more doubloons each.',
'K', 1, 1, false, false, false, false, false, false, false, false, false),
('PirateKingSkull',
'Your treasures are worth 3 more doubloons each.',
'K', 1, 2, false, false, false, false, false, false, false, false, false),
('PirateKingHook',
'Your treasures are worth 3 more doubloons each.',
'K', 1, 3, false, false, false, false, false, false, false, false, false);