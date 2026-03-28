FROM ubuntu:latest
LABEL authors="hercules"

ENTRYPOINT ["top", "-b"]