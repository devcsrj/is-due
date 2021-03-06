# Is Due 

[![Build Status](https://img.shields.io/circleci/project/devcsrj/is-due.svg)]()
[![License](https://img.shields.io/github/license/devcsrj/is-due.svg)]()

Your one-stop app for managing Philippine service provider invoices.

Although service providers in the Philippines have a web portal for accessing invoices, they're either slow, dated, unreliable - usually all of the above. The frustrations in navigating these crappy web portals led me to write this tool to aggregate all of my invoices in one place.

Interestingly enough, this project also made me realize how unsecure and horribly written the said web portals are.

Also, I needed an excuse to write in Kotlin.

## Features

- Fetching paid invoices
- Fetching due invoices

## Running

No user interface yet. This (soon) may be in the form of a cli tool (which may be periodically fetch invoices):

```bash
$ is-due # returns dues for today
```
 
...or a web app with a RESTful endpoint (i.e.: RSS).

## Support

The current project supports fetching data from:

### Internet Service Provider

- [MySky](http://www.mysky.com.ph/)

## Contributing

I have no account for every biller there is. As expected, these companies don't expose public APIs  for app consumption, so the development pace is hindered by the availability of (personal) accounts. If you're subscribe to your own, feel free to submit pull requests! 

(Or give me an access, if you don't mind me seeing your invoices. *wink*)
