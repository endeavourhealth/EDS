/*
	insert test data
*/

insert into sftpreader.instance
(
	instance_id
)
values
(
	'EMIS001'
);

insert into sftpreader.configuration
(
	instance_id,
	local_root_path,
	poll_frequency_seconds
)
values
(
	'EMIS001',
	'/Users/jonny/Code/Local/sftp',
	10
);

insert into sftpreader.configuration_sftp
(
	instance_id,
	hostname,
	port,
	remote_path,
	username,
	client_public_key,
	client_private_key,
	client_private_key_password,
	host_public_key
)
values
(
	'EMIS001',
	'endeavour-sftp.cloudapp.net',
	22,
	'/gpg',
	'test-endeavour',
	'ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABgQCuDY1+QzYDnQntzLbL0JGhtzNhwAf02duBy+slowxMPVz36ehC6bhESGoJptdkoP+jiSnN2sv7wicGnv6HpY21lbQeK2NI12iRCWe/8dx9SELXD5yrhqeA3FyTqKXFsqBOtbORASgXAOM93kRCmIpeRMatCQcpnn4hicYhkMPGVvbbaL7lxbyOBIZRcFSWEuxqVzSZ5ee/sMLDEYHBycGEM5xzvXkS9hjsb3A6/q0vpTEHXg/WrePvzrm2Bw0xYwoB2/y0EdjYFu4BINutcqE5TdJTH/95oaEclADkHTF3e0E2cjIh9AE2JwHArnCO/NzcBhwzdLT1Vs57N9+Xe0rrt1Jka6jaknuhDsGvPyOaE6o4IXF01n9pYlPBAhBzjHvu7cYqgyR9i9sZ2+KDjUnczUuJPoEG4gGQstv2vwZRnWRXScR2wgNUv2TsERDeC/jU4ouCP2oUunzRJziAcK8AElo1f6FjY1Q9DqhibZdNw7G67MQNMd9XpjO4zDRCfY8=',
	'-----BEGIN RSA PRIVATE KEY-----
MIIEowIBAAKCAQEAr+BP2Pd8/a6U+fY6CijAWR+Undbr03aLfyPyo4ld0bw64LZ4
W0yjxxx+Zn2ctO5rFlua9Lr/eY9LurboTbp6rbXT8ddWfMhiaCS+aulp+ZHGzm4w
xLDnvZ6jHZ8DG5Py3rd1Pxb3ypi+ExmAr0CpIAb7gVLUg8ZY3h9rW16SPYEVaq6Y
9EIjR0xclwI2tPmPMG/kViy5fmqjokUVSAb0keNcASOzB4oxCm0chE1j5hFPY+Ct
b3TXyzqtePfNjjukX0YcS/IlLuOnENSgSw3dcA8eVgFpDo9myKjwTzeHe4IkY0rV
zZNlTZ7Naam7rkCj34dQDxHT3dlZu8o20HBwiQIDAQABAoIBAFLZP8PdEawOyW7W
ZpDSkYlqLwALdhuvBBdoP6x50RAzsyiXzCp199Cbw49ULLWuehOWI3CVhjMlJW9X
KiczaQbbWHPcFInDe1fDt091lM5TtWsYzTBahU/1orEZbsVW9Fml4j1N/HwbZ65n
nQ7xTrofj+pjsmhAzywTcOchrZH6rAdj62G2S6nX1XLINO9ljZphL9te2ehwlhce
J3Y0Y5KvNCuYLQOXzy23gBAvCoUumro7I1etMgSnOF1cfvFeiM13LpSnVpK/ZX4n
+5RXEgVHAoCY7YrxTJeQ4yfEAw4ZyovqI0uhZCk0oG/DqoEjyRCTEfh5Vf19dGHo
cENqS4ECgYEA540trVu4+wYDWm98i/SpYYAyRoFfz2SJfq5BxPGlGo79NVsHbCRr
fWf0tb0t8HgC4YtQ+DwIfZDpBjGZhSq72oyz8jlGvsFGE+/9qBODrfc+omB6d4yI
Y3pZYGPkJpqx2NAGPVMuqr5+MZQnyj1pd1UXDd35UqKzm3U+mRB+d4UCgYEAwnI8
Grc8juEhve/ROpODnZoX++19Mr9fh/+4OwGz1Mc83WNWhFoGVR92fv5031PX5k9C
LaXrPmqrXiLGh/Vl9vdBmI+aIBeXZzuHWV3tgfX8iOX5uWt7X9dBE+1jm6LyMFRG
IMF4Kuo0GT5ayN4/LKMmATeRyMDsaU0jpVjEijUCgYA8ESKdSyMEc33hMvSjNdfO
xDvGkH8zRMhqRXgoR+tlpVxIQBH/2r7LrHENyEKnk+pjagmaK7CkKjhY8/r4Uaqj
ukZf+r3AdFDikSpDNLS/J1lnNBHw+1LSQ4X1/FPOM+kY7nX/6Jk9j+cao21jCGVY
oIk3frLT3gMru9F+UYoNJQKBgGUOEYag7GwlPPAFjuuudpyV1RktGAAjzXZUxF6G
R/uxgEReuDwYvWnH7EGb5Qmb/XzIfFBVBqltrppB4IQQWWm8nu620xcSq7EPEwyy
8Bi2ywQq4VooLfplWIEOwtHEIbJUWg3z/ovvJBxqd6wlNRfxwnKnerCm8zbWzEvG
kn5xAoGBAMvqTqrPjQYReiqyyzQ4l0zcEKVbwnRaMejyssrFCKPQ2sCTCM6qhGOA
3SGTiXXZSH9kobPNOHAib7xIkDsWvvJDuRfcZtJnDc908+z+5FkHYISgZw6P4JkC
u8Nrp3k2y4w/OXL3/SdGAhUGYc0IL8FUuWdMH5g2RPXlq/SUiFr1
-----END RSA PRIVATE KEY-----',
	'password',
	'ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABgQCuDY1+QzYDnQntzLbL0JGhtzNhwAf02duBy+slowxMPVz36ehC6bhESGoJptdkoP+jiSnN2sv7wicGnv6HpY21lbQeK2NI12iRCWe/8dx9SELXD5yrhqeA3FyTqKXFsqBOtbORASgXAOM93kRCmIpeRMatCQcpnn4hicYhkMPGVvbbaL7lxbyOBIZRcFSWEuxqVzSZ5ee/sMLDEYHBycGEM5xzvXkS9hjsb3A6/q0vpTEHXg/WrePvzrm2Bw0xYwoB2/y0EdjYFu4BINutcqE5TdJTH/95oaEclADkHTF3e0E2cjIh9AE2JwHArnCO/NzcBhwzdLT1Vs57N9+Xe0rrt1Jka6jaknuhDsGvPyOaE6o4IXF01n9pYlPBAhBzjHvu7cYqgyR9i9sZ2+KDjUnczUuJPoEG4gGQstv2vwZRnWRXScR2wgNUv2TsERDeC/jU4ouCP2oUunzRJziAcK8AElo1f6FjY1Q9DqhibZdNw7G67MQNMd9XpjO4zDRCfY8='
);

insert into sftpreader.configuration_pgp
(
	instance_id,
	file_extension_filter,
	sender_public_key,
	recipient_public_key,
	recipient_private_key,
	recipient_private_key_password
)
values
(
	'EMIS001',
	'.gpg',
	'test-pgp-endeavour-public.asc',
	'test-pgp-endeavour-private.asc',
	'test-pgp-emis-public.asc',
	'password'
);
