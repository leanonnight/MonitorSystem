# -*- coding: utf-8 -*-
'''
#intent      :
#Author      :Michael Jack hu
#start date  : 2019/1/13
#File        : msg.py
#Software    : PyCharm
#finish date :
'''

import time
from twilio.rest import Client

auth_token = '129079f0e0e6c2315266f5aa36a97344'  # 去twilio.com注册账户获取token
account_sid = 'AC9eadad52c303c6daed498fc3cd464bcc'

client = Client(account_sid, auth_token)


def sent_message(phone_number, body):
    mes = client.messages.create(
        from_='+15734754429',  # 填写在active number处获得的号码
        body=body,
        to=phone_number
    )
    print("OK")


if __name__ == '__main__':
    sent_message("+8613507004783", "你好")
