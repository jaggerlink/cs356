# CS 356 Brown Team
## Pacer

This project was written in and designed to run on 32-bit Debian 7.2 Linux.  The download link for that exact version is no longer available, so here is the latest one: http://cdimage.debian.org/debian-cd/current-live/i386/iso-hybrid/debian-live-7.4-i386-xfce-desktop.iso
This iso *should* work, but if it does not then let me know and I can try to get you the old one.  The following dependencies are required for everything to work:
- ecj
- bison
- g++
- openjdk-6-jdk
- gcc
- ant
- perl
- gawk
- gedit
- netbeans

To compile the tool run the following command inside the jikesrvm-3.1.0 directory and answer yes at the prompts.

bin/buildit localhost FastAdaptiveGenImmix_rdSamplingStats -j "/usr/lib/jvm/java-1.6.0-openjdk-i386"

To use the tool open up Netbeans and open the CS356gui project inside the "Netbeans Project" folder and run it.  The tool can only open source code files that are in the same directory as the class file as of now.  A simple race example has been included to test the tool with.