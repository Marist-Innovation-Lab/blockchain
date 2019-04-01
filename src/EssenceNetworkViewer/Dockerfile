FROM python:3.6
MAINTAINER Daniel Gisolfi
EXPOSE 5000

RUN apt-get update -y \
&& apt-get install -y build-essential\
    && pip install --upgrade pip
    

WORKDIR /EssenceMap
COPY ./src .
COPY requirements.txt .


RUN pip install -r requirements.txt


ENTRYPOINT ["python"]
CMD ["server.py"]